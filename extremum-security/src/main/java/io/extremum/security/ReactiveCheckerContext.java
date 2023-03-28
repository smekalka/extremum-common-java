package io.extremum.security;

import reactor.core.publisher.Mono;

import java.security.Principal;

public interface ReactiveCheckerContext {
    Mono<Principal> getCurrentPrincipal();

    Mono<Boolean> currentUserHasOneOf(String... roles);
}
