package io.extremum.security;

import com.google.common.collect.ImmutableList;
import io.extremum.common.modelservices.ModelServices;
import io.extremum.common.utils.ModelUtils;
import io.extremum.security.services.DataAccessChecker;
import io.extremum.sharedmodels.basic.Model;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public final class AccessCheckersDataSecurity implements DataSecurity {
    private final List<DataAccessChecker<?>> checkers;
    private final RoleChecker roleChecker;
    private final PrincipalSource principalSource;

    private final DataSecurityValidation dataSecurityValidation = new DataSecurityValidation();

    private final Operation get = new Get();
    private final Operation patch = new Patch();
    private final Operation remove = new Remove();
    private final Operation watch = new Watch();
    private final Operation create = new Create();

    public AccessCheckersDataSecurity(List<DataAccessChecker<?>> checkers,
                                      RoleChecker roleChecker, PrincipalSource principalSource) {
        this.checkers = ImmutableList.copyOf(checkers);
        this.roleChecker = roleChecker;
        this.principalSource = principalSource;
    }

    @Override
    public void checkGetAllowed(Model model) {
        get.checkDataAccess(model);
    }

    private Optional<DataAccessChecker<Model>> findChecker(Model model) {
        String modelName = ModelUtils.getModelName(model);
        @SuppressWarnings("unchecked")
        DataAccessChecker<Model> castChecker = (DataAccessChecker<Model>) ModelServices.findServiceForModel(
                modelName, checkers);
        return Optional.ofNullable(castChecker);
    }

    @Override
    public void checkPatchAllowed(Model model) {
        patch.checkDataAccess(model);
    }

    @Override
    public void checkRemovalAllowed(Model model) {
        remove.checkDataAccess(model);
    }

    @Override
    public void checkWatchAllowed(Model model) {
        watch.checkDataAccess(model);
    }

    @Override
    public void checkWatchAllowed(Model model, CheckerContext context) {
        watch.checkDataAccess(model, context);
    }

    @Override
    public void checkCreateAllowed(Model model) {
        create.checkDataAccess(model);
    }

    private class SimpleCheckerContext implements CheckerContext {
        @Override
        public Optional<Principal> getCurrentPrincipal() {
            return principalSource.getPrincipal();
        }

        @Override
        public boolean currentUserHasOneOf(String... roles) {
            return roleChecker.currentUserHasOneRoleOf(roles);
        }
    }

    private abstract class Operation {
        void checkDataAccess(Model model) {
            if (model == null) {
                return;
            }

            Optional<DataAccessChecker<Model>> optChecker = findChecker(model);
            dataSecurityValidation.validateModelClassConfig(model, optChecker);

            optChecker.ifPresent(checker -> {
                CheckerContext context = new SimpleCheckerContext();
                if (!allowed(model, checker, context)) {
                    throw new ExtremumAccessDeniedException("Access denied");
                }
            });
        }

        void checkDataAccess(Model model, CheckerContext context) {
            if (model == null) {
                return;
            }

            Optional<DataAccessChecker<Model>> optChecker = findChecker(model);
            dataSecurityValidation.validateModelClassConfig(model, optChecker);

            optChecker.ifPresent(checker -> {
                if (!allowed(model, checker, context)) {
                    throw new ExtremumAccessDeniedException("Access denied");
                }
            });
        }

        abstract boolean allowed(Model model, DataAccessChecker<Model> checker, CheckerContext context);
    }

    private class Get extends Operation {
        @Override
        boolean allowed(Model model, DataAccessChecker<Model> checker, CheckerContext context) {
            return checker.allowedToGet(model, context);
        }
    }

    private class Patch extends Operation {
        @Override
        boolean allowed(Model model, DataAccessChecker<Model> checker, CheckerContext context) {
            return checker.allowedToPatch(model, context);
        }
    }

    private class Remove extends Operation {
        @Override
        boolean allowed(Model model, DataAccessChecker<Model> checker, CheckerContext context) {
            return checker.allowedToRemove(model, context);
        }
    }

    private class Watch extends Operation {
        @Override
        boolean allowed(Model model, DataAccessChecker<Model> checker, CheckerContext context) {
            return checker.allowedToWatch(model, context);
        }
    }

    private class Create extends Operation {
        @Override
        boolean allowed(Model model, DataAccessChecker<Model> checker, CheckerContext context) {
            return checker.allowedToCreate(model, context);
        }
    }
}
