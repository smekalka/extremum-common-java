package io.extremum.security;

import io.extremum.sharedmodels.basic.Model;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
abstract class ConstantReactiveChecker<M extends Model> extends SamePolicyReactiveChecker<M> {
    private final boolean valueToReturn;

    ConstantReactiveChecker(boolean valueToReturn) {
        this.valueToReturn = valueToReturn;
    }

    @Override
    final Mono<Boolean> allowed(M model, ReactiveCheckerContext context) {
        return Mono.just(valueToReturn);
    }
}
