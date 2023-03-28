package io.extremum.security;

import java.security.Principal;
import java.util.Optional;

/**
 * @author rpuch
 */
public interface PrincipalSource {
    Optional<Principal> getPrincipal();
}
