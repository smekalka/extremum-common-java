package io.extremum.everything.services.management;

import io.extremum.everything.services.RemovalService;

/**
 * Uses RemovalService to remove an entity.
 *
 * @author rpuch
 */
final class NonDefaultRemover implements Remover {
    private final RemovalService removalService;

    NonDefaultRemover(RemovalService removalService) {
        this.removalService = removalService;
    }

    @Override
    public void remove(String id) {
        removalService.remove(id);
    }
}
