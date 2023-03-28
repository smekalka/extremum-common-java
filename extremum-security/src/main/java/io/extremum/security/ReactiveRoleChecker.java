package io.extremum.security;

import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactiveRoleChecker {
    Mono<Boolean> currentUserHasOneRoleOf(String... roles);
}
