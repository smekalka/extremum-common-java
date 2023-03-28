package io.extremum.security;

import io.extremum.common.utils.ModelUtils;
import io.extremum.sharedmodels.basic.Model;

import java.util.Optional;

class DataSecurityValidation {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    void validateModelClassConfig(Model model, Optional<?> checker) {
        boolean annotatedWithNoDataSecurity = isAnnotatedWithNoDataSecurity(model);
        throwIfNoCheckerAndNoAnnotation(model, checker, annotatedWithNoDataSecurity);
        throwIfBothCheckerAndAnnotation(model, checker, annotatedWithNoDataSecurity);
    }

    private boolean isAnnotatedWithNoDataSecurity(Model model) {
        return DataSecurityAnnotations.annotatedWithNoDataSecurity(model.getClass());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void throwIfNoCheckerAndNoAnnotation(Model model, Optional<?> checker,
                                                 boolean annotatedWithNoDataSecurity) {
        if (!checker.isPresent() && !annotatedWithNoDataSecurity) {
            String message = String.format(
                    "No DataAccessChecker was found and no @NoDataSecurity annotation exists on '%s'",
                    modelName(model));
            throw new ExtremumSecurityException(message);
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void throwIfBothCheckerAndAnnotation(Model model, Optional<?> checker,
                                                 boolean annotatedWithNoDataSecurity) {
        if (checker.isPresent() && annotatedWithNoDataSecurity) {
            String message = String.format(
                    "Both DataAccessChecker was found and @NoDataSecurity annotation exists on '%s'",
                    modelName(model));
            throw new ExtremumSecurityException(message);
        }
    }

    private String modelName(Model model) {
        return ModelUtils.getModelName(model);
    }
}
