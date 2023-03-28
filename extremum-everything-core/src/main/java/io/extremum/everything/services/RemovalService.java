package io.extremum.everything.services;

/**
 * Service that is used to remove a model from the database for Everything-Everything DELETE operation.
 */
public interface RemovalService extends EverythingEverythingService {
    /**
     * Removes an object by ID. If object with passed ID doesn't exists false will be returned.
     * Otherwise return true
     *
     * @param id of removable object
     */
    void remove(String id);
}
