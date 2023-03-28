package io.extremum.authentication.api;

/**
 * @author rpuch
 */
public interface SecurityIdentity {
    String getExternalId();
    <T extends IdentityExtension> T getExtension();
}
