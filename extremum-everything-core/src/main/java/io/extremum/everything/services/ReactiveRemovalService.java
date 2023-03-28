package io.extremum.everything.services;

import reactor.core.publisher.Mono;

/**
 * Service that is used to remove a model from the database for Everything-Everything DELETE operation reactively.
 */
public interface ReactiveRemovalService extends EverythingEverythingService {
    /**
     * Removes an object by ID. If object with passed ID doesn't exists false will be returned.
     * Otherwise return true
     *
     * @param id of removable object
     */
    Mono<Void> remove(String id);
}
