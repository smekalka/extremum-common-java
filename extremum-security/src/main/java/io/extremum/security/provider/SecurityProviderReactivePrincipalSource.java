package io.extremum.security.provider;

import io.extremum.authentication.api.ReactiveSecurityProvider;
import io.extremum.security.ReactivePrincipalSource;
import reactor.core.publisher.Mono;

import java.security.Principal;

public class SecurityProviderReactivePrincipalSource implements ReactivePrincipalSource {
    private final ReactiveSecurityProvider securityProvider;

    public SecurityProviderReactivePrincipalSource(ReactiveSecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Override
    public Mono<Principal> getPrincipal() {
        return securityProvider.getPrincipal();
    }
}
