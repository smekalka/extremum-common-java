package io.extremum.common.reactive;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@link Reactifier} implementation that uses an isolated {@link reactor.core.scheduler.Scheduler}
 * (that in turn uses an isolated thread pool). This, in turn, allows to isolate pseudo-reactiveness
 * from the true one.
 */
@RequiredArgsConstructor
public class IsolatedSchedulerReactifier implements Reactifier {
    private final Scheduler scheduler;

    @Override
    public <T> Mono<T> mono(Supplier<? extends T> objectSupplier) {
        return Mono.fromCallable(objectSupplier::get)
                .subscribeOn(scheduler)
                .map(Function.identity());
    }

    @Override
    public <T> Flux<T> flux(Supplier<? extends Iterable<? extends T>> iterableSupplier) {
        return Mono.fromCallable(iterableSupplier::get)
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(scheduler)
                .map(Function.identity());
    }
}
