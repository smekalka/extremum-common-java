package io.extremum.security.provider;

import io.extremum.authentication.api.ReactiveSecurityProvider;
import io.extremum.security.ReactiveRoleChecker;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public class SecurityProviderReactiveRoleChecker implements ReactiveRoleChecker {
    private final ReactiveSecurityProvider securityProvider;

    public SecurityProviderReactiveRoleChecker(ReactiveSecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Override
    public Mono<Boolean> currentUserHasOneRoleOf(String... roles) {
        return securityProvider.hasAnyOfRoles(roles);
    }
}
