package io.extremum.security.provider;

import io.extremum.authentication.api.SecurityProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContextHolderSecurityProvider implements SecurityProvider {

    @Override
    public Optional<Principal> getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Principal principal = (Principal) authentication.getPrincipal();

        return Optional.of(principal);
    }

    @Override
    public boolean hasAnyOfRoles(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<String> authorities = authentication
                .getAuthorities()
                .stream()
                .map(grantedAuthority -> grantedAuthority.toString().toLowerCase())
                .collect(Collectors.toList());

        return Arrays.stream(roles).distinct().anyMatch(o -> authorities.contains(o.toLowerCase()));
    }

    @Override
    public <T> T getSessionExtension() {
        return null;
    }

    @Override
    public <T> T getIdentityExtension() {
        return null;
    }
}
