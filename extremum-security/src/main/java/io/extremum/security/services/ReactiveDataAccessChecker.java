package io.extremum.security.services;

import io.extremum.common.modelservices.ModelService;
import io.extremum.security.CheckerContext;
import io.extremum.security.ReactiveCheckerContext;
import io.extremum.sharedmodels.basic.Model;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactiveDataAccessChecker<M extends Model> extends ModelService {
    Mono<Boolean> allowedToGet(M model, ReactiveCheckerContext context);

    Mono<Boolean> allowedToPatch(M model, ReactiveCheckerContext context);

    Mono<Boolean> allowedToRemove(M model, ReactiveCheckerContext context);

    Mono<Boolean> allowedToWatch(M model, ReactiveCheckerContext context);
}
