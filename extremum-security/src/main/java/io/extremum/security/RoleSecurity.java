package io.extremum.security;

import io.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public interface RoleSecurity {
    void checkGetAllowed(Descriptor id) throws ExtremumAccessDeniedException, ExtremumSecurityException;

    void checkPatchAllowed(Descriptor id) throws ExtremumAccessDeniedException, ExtremumSecurityException;

    void checkRemovalAllowed(Descriptor id) throws ExtremumAccessDeniedException, ExtremumSecurityException;

    void checkWatchAllowed(Descriptor id) throws ExtremumAccessDeniedException, ExtremumSecurityException;
}
