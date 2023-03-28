package io.extremum.security;

import io.extremum.sharedmodels.basic.Model;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public class AllowEverythingForDataAccessReactively implements ReactiveDataSecurity {
    @Override
    public Mono<Void> checkGetAllowed(Model model) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> checkPatchAllowed(Model model) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> checkRemovalAllowed(Model model) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> checkWatchAllowed(Model model) {
        return Mono.empty();
    }
}
