package io.extremum.security;

import io.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public final class AllowEverythingForRoleAccess implements RoleSecurity {
    @Override
    public void checkGetAllowed(Descriptor id) {
        // allow anything
    }

    @Override
    public void checkPatchAllowed(Descriptor id) {
        // allow anything
    }

    @Override
    public void checkRemovalAllowed(Descriptor id) {
        // allow anything
    }

    @Override
    public void checkWatchAllowed(Descriptor id) throws ExtremumSecurityException {
        // allow anything
    }
}
