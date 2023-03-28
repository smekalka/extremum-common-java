package io.extremum.security;

import io.extremum.sharedmodels.basic.Model;

public class AllowEverythingForDataAccess implements DataSecurity {
    @Override
    public void checkGetAllowed(Model model) {
        // allow anything
    }

    @Override
    public void checkPatchAllowed(Model model) {
        // allow anything
    }

    @Override
    public void checkRemovalAllowed(Model model) {
        // allow anything
    }

    @Override
    public void checkWatchAllowed(Model model) {
        // allow anything
    }

    @Override
    public void checkCreateAllowed(Model model) {
        // allow anything
    }

    @Override
    public void checkWatchAllowed(Model model, CheckerContext context) {
        // allow anything
    }
}
