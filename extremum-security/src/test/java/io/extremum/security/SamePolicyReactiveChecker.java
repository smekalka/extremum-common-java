package io.extremum.security;

import io.extremum.security.services.ReactiveDataAccessChecker;
import io.extremum.sharedmodels.basic.Model;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
abstract class SamePolicyReactiveChecker<M extends Model> implements ReactiveDataAccessChecker<M> {
    public final Mono<Boolean> allowedToGet(M model, ReactiveCheckerContext context) {
        return allowed(model, context);
    }

    public final Mono<Boolean> allowedToPatch(M model, ReactiveCheckerContext context) {
        return allowed(model, context);
    }

    public final Mono<Boolean> allowedToRemove(M model, ReactiveCheckerContext context) {
        return allowed(model, context);
    }

    public Mono<Boolean> allowedToWatch(M model, ReactiveCheckerContext context) {
        return allowed(model, context);
    }

    abstract Mono<Boolean> allowed(M model, ReactiveCheckerContext context);
}
