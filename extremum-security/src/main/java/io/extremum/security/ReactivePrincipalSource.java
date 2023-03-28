package io.extremum.security;

import reactor.core.publisher.Mono;

import java.security.Principal;

public interface ReactivePrincipalSource {
    Mono<Principal> getPrincipal();
}
