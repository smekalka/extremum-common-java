package io.extremum.authentication.api;

/**
 * @author rpuch
 */
public interface IdentityFinder {
    SecurityIdentity findByPrincipalId(String principalId);
}
