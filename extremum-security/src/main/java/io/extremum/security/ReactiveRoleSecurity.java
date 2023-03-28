package io.extremum.security;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactiveRoleSecurity {
    Mono<Void> checkGetAllowed(Descriptor id) throws ExtremumAccessDeniedException, ExtremumSecurityException;

    Mono<Void> checkPatchAllowed(Descriptor id) throws ExtremumAccessDeniedException, ExtremumSecurityException;

    Mono<Void> checkRemovalAllowed(Descriptor id) throws ExtremumAccessDeniedException, ExtremumSecurityException;

    Mono<Void> checkWatchAllowed(Descriptor id) throws ExtremumAccessDeniedException, ExtremumSecurityException;
}
