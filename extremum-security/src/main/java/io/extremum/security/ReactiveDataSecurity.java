package io.extremum.security;

import io.extremum.sharedmodels.basic.Model;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactiveDataSecurity {
    Mono<Void> checkGetAllowed(Model model);

    Mono<Void> checkPatchAllowed(Model model);

    Mono<Void> checkRemovalAllowed(Model model);

    Mono<Void> checkWatchAllowed(Model model);
}
