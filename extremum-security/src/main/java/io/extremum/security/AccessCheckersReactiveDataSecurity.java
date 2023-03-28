package io.extremum.security;

import com.google.common.collect.ImmutableList;
import io.extremum.common.modelservices.ModelServices;
import io.extremum.common.utils.ModelUtils;
import io.extremum.security.services.ReactiveDataAccessChecker;
import io.extremum.sharedmodels.basic.Model;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public final class AccessCheckersReactiveDataSecurity implements ReactiveDataSecurity {
    private final List<ReactiveDataAccessChecker<?>> checkers;
    private final ReactiveRoleChecker roleChecker;
    private final ReactivePrincipalSource principalSource;

    private final DataSecurityValidation dataSecurityValidation = new DataSecurityValidation();

    private final Operation get = new Get();
    private final Operation patch = new Patch();
    private final Operation remove = new Remove();
    private final Operation watch = new Watch();

    public AccessCheckersReactiveDataSecurity(List<ReactiveDataAccessChecker<?>> checkers,
                                              ReactiveRoleChecker roleChecker,
                                              ReactivePrincipalSource principalSource) {
        this.checkers = ImmutableList.copyOf(checkers);
        this.roleChecker = roleChecker;
        this.principalSource = principalSource;
    }

    @Override
    public Mono<Void> checkGetAllowed(Model model) {
        return get.checkDataAccess(model);
    }

    private Optional<ReactiveDataAccessChecker<Model>> findChecker(Model model) {
        String modelName = ModelUtils.getModelName(model);
        @SuppressWarnings("unchecked")
        ReactiveDataAccessChecker<Model> checker = (ReactiveDataAccessChecker<Model>) ModelServices.findServiceForModel(
                modelName, checkers);
        return Optional.ofNullable(checker);
    }

    @Override
    public Mono<Void> checkPatchAllowed(Model model) {
        return patch.checkDataAccess(model);
    }

    @Override
    public Mono<Void> checkRemovalAllowed(Model model) {
        return remove.checkDataAccess(model);
    }

    @Override
    public Mono<Void> checkWatchAllowed(Model model) {
        return watch.checkDataAccess(model);
    }

    private class SimpleCheckerContext implements ReactiveCheckerContext {
        @Override
        public Mono<Principal> getCurrentPrincipal() {
            return principalSource.getPrincipal();
        }

        @Override
        public Mono<Boolean> currentUserHasOneOf(String... roles) {
            return roleChecker.currentUserHasOneRoleOf(roles);
        }
    }

    private abstract class Operation {
        Mono<Void> checkDataAccess(Model model) {
            if (model == null) {
                return Mono.empty();
            }

            return Mono.defer(() -> checkAccessOnNonNullModel(model));
        }

        private Mono<Void> checkAccessOnNonNullModel(Model model) {
            Optional<ReactiveDataAccessChecker<Model>> optChecker = findChecker(model);
            dataSecurityValidation.validateModelClassConfig(model, optChecker);

            if (optChecker.isPresent()) {
                ReactiveCheckerContext context = new SimpleCheckerContext();
                return allowed(model, optChecker.get(), context)
                        .doOnNext(allowed -> {
                            if (!allowed) {
                                throw new ExtremumAccessDeniedException("Access denied");
                            }
                        })
                        .then();
            }

            return Mono.empty();
        }

        abstract Mono<Boolean> allowed(Model model, ReactiveDataAccessChecker<Model> checker,
                                       ReactiveCheckerContext context);
    }

    private class Get extends Operation {
        @Override
        Mono<Boolean> allowed(Model model, ReactiveDataAccessChecker<Model> checker, ReactiveCheckerContext context) {
            return checker.allowedToGet(model, context);
        }
    }

    private class Patch extends Operation {
        @Override
        Mono<Boolean> allowed(Model model, ReactiveDataAccessChecker<Model> checker, ReactiveCheckerContext context) {
            return checker.allowedToPatch(model, context);
        }
    }

    private class Remove extends Operation {
        @Override
        Mono<Boolean> allowed(Model model, ReactiveDataAccessChecker<Model> checker, ReactiveCheckerContext context) {
            return checker.allowedToRemove(model, context);
        }
    }

    private class Watch extends Operation {
        @Override
        Mono<Boolean> allowed(Model model, ReactiveDataAccessChecker<Model> checker, ReactiveCheckerContext context) {
            return checker.allowedToWatch(model, context);
        }
    }
}
