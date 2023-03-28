package io.extremum.common.descriptorpool;

import com.google.common.util.concurrent.Uninterruptibles;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.synchronizedList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BufferedReactiveFactoryTest {
    @Mock
    private Allocator<String> stringAllocator;

    private BufferedReactiveFactory<String> factory;

    @BeforeEach
    void initMocks() {
        lenient().when(stringAllocator.allocate(3))
                .thenReturn(Arrays.asList("one", "two", "three"));
    }

    @AfterEach
    void shutdownFactory() {
        if (factory != null) {
            factory.shutdown();
        }
    }

    @Test
    void shouldReturnWhatAllocatorReturns() {
        factory = buildFactory();

        assertThat(factory.get().block(), is("one"));
        assertThat(factory.get().block(), is("two"));
        assertThat(factory.get().block(), is("three"));
        assertThat(factory.get().block(), is("one"));
    }

    private BufferedReactiveFactory<String> buildFactory() {
        BufferedReactiveFactoryConfig config = BufferedReactiveFactoryConfig.builder()
                .batchSize(3)
                .startAllocationThreshold(0.1f)
                .maxClientsToWaitForAllocation(1000)
                .checkForAllocationEachMillis(1)
                .build();
        return new BufferedReactiveFactory<>(config, stringAllocator);
    }

    @Test
    void shouldNotAllocateIfNothingIsRequestedYet() throws InterruptedException {
        factory = buildFactory();

        waitToLetFactoryMakeAnAllocation();

        verify(stringAllocator, never()).allocate(anyInt());
    }

    private void waitToLetFactoryMakeAnAllocation() throws InterruptedException {
        Thread.sleep(100);
    }

    @Test
    void shouldRejectRequestsWhenWaitingCapacityIsExhausted() {
        /*
         * Here, 3 clients are making requests at the same time. One gets executed,
         * another one is put in executor queue (allowed by maxClientsToWaitForAllocation(1),
         * the third one fails with RejectedExecutionException.
         */

        factory = new BufferedReactiveFactory<>(configWithMax1ClientAllowedToWait(), slowAllocator());

        AtomicInteger successCounter = new AtomicInteger(0);
        List<Throwable> exceptions = new CopyOnWriteArrayList<>();

        Runnable task = () -> factory.get()
                .doOnNext(x -> successCounter.incrementAndGet())
                .doOnError(exceptions::add)
                .block();
        List<Thread> threads = IntStream.range(0, 3)
                .mapToObj(i -> new Thread(task))
                .collect(Collectors.toList());

        threads.forEach(Thread::start);
        threads.forEach(this::joinThread);

        assertThatTwoTasksExecutedSuccessfully(successCounter);
        assertThatThereIsOneRejectedExecutionException(exceptions);
    }

    private BufferedReactiveFactoryConfig configWithMax1ClientAllowedToWait() {
        return BufferedReactiveFactoryConfig.builder()
                    .batchSize(100)
                    .maxClientsToWaitForAllocation(1)
                    .build();
    }

    @NotNull
    private Allocator<String> slowAllocator() {
        return quantityToAllocate -> {
            Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
            return IntStream.range(0, quantityToAllocate)
                    .mapToObj(Integer::toString)
                    .collect(Collectors.toList());
        };
    }

    private void joinThread(Thread t) {
        try {
            t.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void assertThatTwoTasksExecutedSuccessfully(AtomicInteger successCounter) {
        assertThat(successCounter.get(), is(2));
    }

    private void assertThatThereIsOneRejectedExecutionException(List<Throwable> exceptions) {
        assertThat(exceptions, hasSize(1));
        assertThat(exceptions.get(0), is(instanceOf(RejectedExecutionException.class)));
    }

    @Test
    void givenFactoryIsShutDown_whenGettingAnElement_thenAnExceptionShouldBeReturned() {
        factory = buildFactory();
        factory.shutdown();

        Mono<String> mono = factory.get();

        StepVerifier.create(mono)
                .expectError(FactoryClosedException.class)
                .verify();
    }

    @Test
    void givenFactoryIsShutDownAndLeftoversDestroyed_whenGettingAnElement_thenAnExceptionShouldBeReturned()
            throws InterruptedException {
        factory = buildFactory();
        factory.shutdownAndDestroyLeftovers(1, TimeUnit.SECONDS, leftovers -> {});

        Mono<String> mono = factory.get();

        StepVerifier.create(mono)
                .expectError(FactoryClosedException.class)
                .verify();
    }

    @Test
    void givenFactoryIsShutDownAndLeftoversDestroyed_whenShuttingDown_thenNothingShouldHappen()
            throws InterruptedException {
        factory = buildFactory();
        factory.shutdownAndDestroyLeftovers(1, TimeUnit.SECONDS, leftovers -> {});

        factory.shutdown();
    }

    @Test
    void givenFactoryIsShutDown_whenShuttingDownAndDestoryingLeftovers_thenNothingShouldHappen()
            throws InterruptedException {
        factory = buildFactory();
        factory.shutdown();

        factory.shutdownAndDestroyLeftovers(1, TimeUnit.SECONDS, leftovers -> {});
    }

    @Test
    void whenShuttingDownAndDestroyingLeftovers_thenLeftoversShouldBeDestroyedWithDestoryer()
            throws InterruptedException {
        factory = buildFactory();
        factory.get().block();

        List<String> leftover = synchronizedList(new ArrayList<>());
        factory.shutdownAndDestroyLeftovers(1, TimeUnit.SECONDS, leftover::addAll);

        assertThat(leftover, is(equalTo(Arrays.asList("two", "three"))));
    }

    @Test
    void givenAllocationIsInProgress_whenShuttingDownAndDestoryingLeftovers_thenLeftoversShouldBeDestroyedWithDestoryer()
            throws InterruptedException {
        BufferedReactiveFactoryConfig config = BufferedReactiveFactoryConfig.builder()
                .batchSize(3)
                .checkForAllocationEachMillis(1)
                .build();
        factory = new BufferedReactiveFactory<>(config, new SlowAfterFirstAllocation());

        takeAllFromFastBatch();
        requestFirstFromSlowBatch();

        Thread.sleep(20);

        List<String> leftover = synchronizedList(new ArrayList<>());
        factory.shutdownAndDestroyLeftovers(1, TimeUnit.SECONDS, leftover::addAll);

        assertThat(leftover,
                either(equalTo(Arrays.asList("two", "three")))
                        .or(equalTo(Arrays.asList("one", "two", "three"))));
    }

    private void takeAllFromFastBatch() {
        for (int i = 0; i < 3; i++) {
            factory.get().block();
        }
    }

    private void requestFirstFromSlowBatch() {
        factory.get().subscribe();
    }

    private static class SlowAfterFirstAllocation implements Allocator<String> {
        private volatile boolean allocatedAnything = false;

        @Override
        public List<String> allocate(int quantityToAllocate) {
            if (allocatedAnything) {
                Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
            }
            allocatedAnything = true;
            return Arrays.asList("one", "two", "three");
        }
    }
}