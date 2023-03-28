package io.extremum.security;

import io.extremum.security.services.DataAccessChecker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a model class is annotated with @NoDataSecurity, it means
 * that no security checks are made using {@link DataAccessChecker}.
 *
 * This annotation has no relation to Role Security. That is, if
 * a class is annotated with @NoDataSecurity, the @{@link ExtremumRequiredRoles}
 * is still in play.
 *
 * For every model class accessible via Everything-Everything framework,
 * exactly one of the following must be in place:
 * - @NoDataSecurity annotation on the model class
 * - DataAccessChecker in application context
 *
 * If there is neither, or there are both, this is an error.
 *
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NoDataSecurity {
}
