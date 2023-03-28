package io.extremum.authentication.api;

import reactor.core.publisher.Mono;

import java.security.Principal;

public interface ReactiveSecurityProvider {

    Mono<Principal> getPrincipal();

    Mono<Boolean> hasAnyOfRoles(String... roles);

    <T> Mono<T> getSessionExtension();

    <T> Mono<T> getIdentityExtension();
}
