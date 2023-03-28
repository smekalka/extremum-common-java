package io.extremum.security.provider;

import io.extremum.authentication.api.SecurityProvider;
import io.extremum.security.PrincipalSource;

import java.security.Principal;
import java.util.Optional;


public class SecurityProviderPrincipalSource implements PrincipalSource {
    private final SecurityProvider securityProvider;

    public SecurityProviderPrincipalSource(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Override
    public Optional<Principal> getPrincipal() {
        return securityProvider.getPrincipal();
    }
}
