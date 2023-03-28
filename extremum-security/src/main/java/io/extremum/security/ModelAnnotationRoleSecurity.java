package io.extremum.security;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.support.ModelClasses;
import io.extremum.common.utils.AnnotationUtils;
import io.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public class ModelAnnotationRoleSecurity implements RoleSecurity {
    private final RoleChecker roleChecker;
    private final ModelClasses modelClasses;

    private final Operation get = new Get();
    private final Operation patch = new Patch();
    private final Operation remove = new Remove();
    private final Operation watch = new Watch();

    public ModelAnnotationRoleSecurity(RoleChecker roleChecker,
            ModelClasses modelClasses) {
        this.roleChecker = roleChecker;
        this.modelClasses = modelClasses;
    }

    @Override
    public void checkGetAllowed(Descriptor id) {
        get.throwIfNoRolesFor(id);
    }

    @Override
    public void checkPatchAllowed(Descriptor id) {
        patch.throwIfNoRolesFor(id);
    }

    @Override
    public void checkRemovalAllowed(Descriptor id) {
        remove.throwIfNoRolesFor(id);
    }

    @Override
    public void checkWatchAllowed(Descriptor id) throws ExtremumSecurityException {
        watch.throwIfNoRolesFor(id);
    }

    private abstract class Operation {
        private final ExtremumRequiredRolesParser annotationParser = new ExtremumRequiredRolesParser();

        void throwIfNoRolesFor(Descriptor id) {
            Class<Model> modelClass = modelClasses.getClassByModelName(id.getModelType());

            ExtremumRequiredRoles extremumRequiredRoles = AnnotationUtils.findAnnotationDirectlyOrUnderProxy(
                    ExtremumRequiredRoles.class, modelClass);
            if (extremumRequiredRoles == null) {
                throw new ExtremumSecurityException(
                        String.format("Security is not configured for '%s'", id.getModelType()));
            }
            ExtremumRequiredRolesConfig config = annotationParser.parse(extremumRequiredRoles);
            String[] roles = extractRoles(config);

            if (roles.length == 0) {
                String message = String.format("Security is not configured for '%s' for %s operation",
                        id.getModelType(), name());
                throw new ExtremumSecurityException(message);
            }

            if (!roleChecker.currentUserHasOneRoleOf(roles)) {
                throw new ExtremumAccessDeniedException("Access denied");
            }
        }

        abstract String[] extractRoles(ExtremumRequiredRolesConfig config);

        abstract String name();
    }

    private class Get extends Operation {
        @Override
        String[] extractRoles(ExtremumRequiredRolesConfig config) {
            return config.rolesForGet();
        }

        @Override
        String name() {
            return "get";
        }
    }

    private class Patch extends Operation {
        @Override
        String[] extractRoles(ExtremumRequiredRolesConfig config) {
            return config.rolesForPatch();
        }

        @Override
        String name() {
            return "patch";
        }
    }

    private class Remove extends Operation {
        @Override
        String[] extractRoles(ExtremumRequiredRolesConfig config) {
            return config.rolesForRemove();
        }

        @Override
        String name() {
            return "remove";
        }
    }

    private class Watch extends Operation {
        @Override
        String[] extractRoles(ExtremumRequiredRolesConfig config) {
            return config.rolesForWatch();
        }

        @Override
        String name() {
            return "watch";
        }
    }
}
