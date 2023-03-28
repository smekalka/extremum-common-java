package io.extremum.security;

import java.security.Principal;
import java.util.Optional;

/**
 * @author rpuch
 */
public interface CheckerContext {
    Optional<Principal> getCurrentPrincipal();

    boolean currentUserHasOneOf(String... roles);
}
