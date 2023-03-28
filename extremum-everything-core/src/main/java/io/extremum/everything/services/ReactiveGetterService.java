package io.extremum.everything.services;

import io.extremum.sharedmodels.basic.Model;
import reactor.core.publisher.Mono;

/**
 * Service that is used to obtain a model from the database reactively for Everything-Everything
 * GET operation and PATCH operation, and to obtain collection host.
 *
 * @param <M> model type
 */
public interface ReactiveGetterService<M extends Model> extends EverythingEverythingService {
    /**
     * Finds a model by ID reactively
     *
     * @param id descriptor internal ID
     * @return found object mono
     */
    Mono<M> get(String id);
}
