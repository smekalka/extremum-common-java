package io.extremum.security;

import reactor.core.publisher.Mono;

public class ReactiveAllowAnyRoleChecker implements ReactiveRoleChecker {
    @Override
    public Mono<Boolean> currentUserHasOneRoleOf(String... roles) {
        return Mono.just(true);
    }
}
