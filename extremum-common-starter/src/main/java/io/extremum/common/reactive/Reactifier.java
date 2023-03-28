package io.extremum.common.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * A converter that is able to 'turn' blocking computations to reactive ones.
 * No magic is involved, so it does what it can do, but no more.
 * For example, a standard implementation may use the same Scheduler for all
 * computations to isolate 'bad' reactiveness from the 'good' one.
 */
public interface Reactifier {
    /**
     * Creates a {@link Mono} that produces reactively (as possible) the result
     * that would be supplied blockingly by the given {@link Supplier}.
     *
     * @param objectSupplier    the supplier
     * @param <T> type of the produced object
     * @return Mono
     */
    <T> Mono<T> mono(Supplier<? extends T> objectSupplier);

    /**
     * Creates a {@link Flux} that produces reactively (as possible) the result
     * that would be supplied blockingly by the given {@link Supplier}.
     *
     * @param iterableSupplier    the supplier
     * @param <T> type of the produced object
     * @return Mono
     */
    <T> Flux<T> flux(Supplier<? extends Iterable<? extends T>> iterableSupplier);
}
