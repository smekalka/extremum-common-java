package io.extremum.everything.services.management;

import io.extremum.everything.services.ReactiveRemovalService;
import reactor.core.publisher.Mono;

/**
 * Uses ReactiveRemovalService to remove an entity.
 *
 * @author rpuch
 */
final class NonDefaultReactiveRemover implements ReactiveRemover {
    private final ReactiveRemovalService removalService;

    NonDefaultReactiveRemover(ReactiveRemovalService removalService) {
        this.removalService = removalService;
    }

    @Override
    public Mono<Void> remove(String id) {
        return removalService.remove(id);
    }
}
