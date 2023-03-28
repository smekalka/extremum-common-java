package io.extremum.security.provider;

import io.extremum.authentication.api.SecurityProvider;
import io.extremum.security.RoleChecker;

/**
 * @author rpuch
 */
public class SecurityProviderRoleChecker implements RoleChecker {
    private final SecurityProvider securityProvider;

    public SecurityProviderRoleChecker(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Override
    public boolean currentUserHasOneRoleOf(String... roles) {
        return securityProvider.hasAnyOfRoles(roles);
    }
}
