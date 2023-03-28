package io.extremum.everything.collection;

import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.OptionalLong;
import java.util.function.Function;

/**
 * @author rpuch
 */
public interface CollectionFragment<T> {
    static <T> CollectionFragment<T> emptyWithZeroTotal() {
        return new CollectionFragmentImpl<>(Collections.emptyList(), 0);
    }

    static <T> CollectionFragment<T> forCompleteCollection(Collection<T> collection) {
        return new CollectionFragmentImpl<>(collection, collection.size());
    }

    static <T> CollectionFragment<T> forFragment(Collection<T> fragmentElements, long total) {
        return new CollectionFragmentImpl<>(fragmentElements, total);
    }

    static <T> CollectionFragment<T> forUnknownSize(Collection<T> fragmentElements) {
        return new CollectionFragmentImpl<>(fragmentElements, null);
    }

    Collection<T> elements();

    OptionalLong total();

    <U> CollectionFragment<U> map(Function<? super T, ? extends U> mapper);

    <U> Mono<CollectionFragment<U>> mapReactively(Function<? super T, ? extends Mono<? extends U>> mapper);
}
