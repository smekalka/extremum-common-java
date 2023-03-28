package io.extremum.security;

import io.extremum.sharedmodels.basic.Model;

public interface DataSecurity {
    void checkGetAllowed(Model model);

    void checkPatchAllowed(Model model);

    void checkRemovalAllowed(Model model);

    void checkWatchAllowed(Model model);

    void checkWatchAllowed(Model model, CheckerContext context);

    void checkCreateAllowed(Model model);
}
