package io.extremum.security;

import io.extremum.authentication.api.ReactiveSecurityProvider;
import reactor.core.publisher.Mono;

import java.security.Principal;

public class NullReactiveSecurityProvider implements ReactiveSecurityProvider {
    @Override
    public Mono<Principal> getPrincipal() {
        return Mono.empty();
    }

    @Override
    public Mono<Boolean> hasAnyOfRoles(String... roles) {
        return Mono.just(false);
    }

    @Override
    public <T> Mono<T> getSessionExtension() {
        return Mono.empty();
    }

    @Override
    public <T> Mono<T> getIdentityExtension() {
        return Mono.empty();
    }
}
