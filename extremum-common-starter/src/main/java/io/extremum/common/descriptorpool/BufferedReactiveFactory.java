package io.extremum.common.descriptorpool;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class BufferedReactiveFactory<T> implements ReactiveSupplier<T> {
    private final BufferedReactiveFactoryConfig config;
    private final Allocator<T> allocator;
    private final Scheduler schedulerToWaitForAllocation;

    private final BlockingQueue<T> elements;
    private final RunOnFlagOrPeriodically allocation;

    private volatile boolean closed = false;

    public BufferedReactiveFactory(BufferedReactiveFactoryConfig config, Allocator<T> allocator) {
        config.validate();

        this.config = config;
        this.allocator = allocator;

        elements = new ArrayBlockingQueue<>(config.getBatchSize() * 2);

        ExecutorService executorService = newBoundedSingleThreadExecutor(config.getMaxClientsToWaitForAllocation());
        schedulerToWaitForAllocation = Schedulers.fromExecutorService(executorService);

        allocation = new RunOnFlagOrPeriodically(config.getCheckForAllocationEachMillis(), new AllocateConditionally());
    }

    private ThreadPoolExecutor newBoundedSingleThreadExecutor(int maxClientsToWaitForAllocation) {
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(maxClientsToWaitForAllocation),
                new CustomizableThreadFactory("wait-for-allocation-"));
    }

    @Override
    public Mono<T> get() {
        return Mono.defer(() -> {
            if (closed) {
                return Mono.error(new FactoryClosedException("Already closed"));
            }

            T value = elements.poll();
            if (value != null) {
                return Mono.just(value);
            }

            return Mono.fromCallable(() -> {
                requestAllocationIfTooFewLeft();
                return elements.take();
            }).subscribeOn(schedulerToWaitForAllocation);
        });
    }

    private void requestAllocationIfTooFewLeft() {
        if (tooFewLeft()) {
            requestAllocation();
        }
    }

    private boolean tooFewLeft() {
        return (float) elements.size() / config.getBatchSize() < config.getStartAllocationThreshold();
    }

    private void requestAllocation() {
        allocation.raiseFlag();
    }

    @PreDestroy
    public void shutdown() {
        closed = true;
        allocation.shutdown();
        schedulerToWaitForAllocation.dispose();
    }

    public void shutdownAndDestroyLeftovers(long timeout, TimeUnit timeUnit, BatchDestroyer<T> destroyer) throws InterruptedException {
        closed = true;
        allocation.shutdownAndWait(timeout, timeUnit);
        schedulerToWaitForAllocation.dispose();

        destroyLeftovers(destroyer);
    }

    private void destroyLeftovers(BatchDestroyer<T> destroyer) {
        List<T> leftovers = new ArrayList<>();
        elements.drainTo(leftovers);
        destroyer.destroy(leftovers);
    }

    private class AllocateConditionally implements Runnable {
        @Override
        public void run() {
            if (tooFewLeft()) {
                allocate();
            }
        }

        private void allocate() {
            List<T> newElements = allocator.allocate(config.getBatchSize());
            elements.addAll(newElements);
        }
    }
}
