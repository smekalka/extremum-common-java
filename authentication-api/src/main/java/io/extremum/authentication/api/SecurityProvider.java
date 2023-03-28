package io.extremum.authentication.api;

import java.security.Principal;
import java.util.Optional;

public interface SecurityProvider {

    Optional<Principal> getPrincipal();

    boolean hasAnyOfRoles(String... roles);

    <T> T getSessionExtension();

    <T> T getIdentityExtension();
}
