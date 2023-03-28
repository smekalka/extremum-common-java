package io.extremum.security;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public final class AllowEverythingForRoleAccessReactively implements ReactiveRoleSecurity {
    @Override
    public Mono<Void> checkGetAllowed(Descriptor id) throws ExtremumAccessDeniedException, ExtremumSecurityException {
        return Mono.empty();
    }

    @Override
    public Mono<Void> checkPatchAllowed(Descriptor id) throws ExtremumAccessDeniedException, ExtremumSecurityException {
        return Mono.empty();
    }

    @Override
    public Mono<Void> checkRemovalAllowed(Descriptor id) throws ExtremumAccessDeniedException, ExtremumSecurityException {
        return Mono.empty();
    }

    @Override
    public Mono<Void> checkWatchAllowed(Descriptor id) throws ExtremumAccessDeniedException, ExtremumSecurityException {
        return Mono.empty();
    }
}

