package io.extremum.everything.services.management;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.everything.services.ReactiveGetterService;
import reactor.core.publisher.Mono;

/**
 * Uses ReactiveGetterService to get an entity.
 *
 * @author rpuch
 */
final class NonDefaultReactiveGetter implements ReactiveGetter {
    private final ReactiveGetterService<Model> getterService;

    NonDefaultReactiveGetter(ReactiveGetterService<Model> getterService) {
        this.getterService = getterService;
    }

    @Override
    public Mono<Model> get(String id) {
        return getterService.get(id);
    }
}
