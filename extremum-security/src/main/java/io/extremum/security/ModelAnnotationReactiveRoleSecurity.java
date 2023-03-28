package io.extremum.security;

import io.extremum.common.support.ModelClasses;
import io.extremum.common.utils.AnnotationUtils;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public class ModelAnnotationReactiveRoleSecurity implements ReactiveRoleSecurity {
    private final ReactiveRoleChecker roleChecker;
    private final ModelClasses modelClasses;

    private final Operation get = new Get();
    private final Operation patch = new Patch();
    private final Operation remove = new Remove();
    private final Operation watch = new Watch();

    public ModelAnnotationReactiveRoleSecurity(ReactiveRoleChecker roleChecker,
                                               ModelClasses modelClasses) {
        this.roleChecker = roleChecker;
        this.modelClasses = modelClasses;
    }

    @Override
    public Mono<Void> checkGetAllowed(Descriptor id) {
        return get.errorIfNoRolesFor(id);
    }

    @Override
    public Mono<Void> checkPatchAllowed(Descriptor id) {
        return patch.errorIfNoRolesFor(id);
    }

    @Override
    public Mono<Void> checkRemovalAllowed(Descriptor id) {
        return remove.errorIfNoRolesFor(id);
    }

    @Override
    public Mono<Void> checkWatchAllowed(Descriptor id) throws ExtremumSecurityException {
        return watch.errorIfNoRolesFor(id);
    }

    private abstract class Operation {
        private final ExtremumRequiredRolesParser annotationParser = new ExtremumRequiredRolesParser();

        Mono<Void> errorIfNoRolesFor(Descriptor id) {
            return Mono.just(id)
                    .map(this::getRequiredRoles)
                    .flatMap(roleChecker::currentUserHasOneRoleOf)
                    .doOnNext(hasRoles -> {
                        if (!hasRoles) {
                            throw new ExtremumAccessDeniedException("Access denied");
                        }
                    })
                    .then();
        }

        private String[] getRequiredRoles(Descriptor id) {
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
            return roles;
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
