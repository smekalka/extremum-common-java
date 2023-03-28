package io.extremum.sharedmodels.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OnePrimaryContactAllowedValidator.class)
public @interface OnePrimaryContactAllowed {
    String message() default "Only one primary contact allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
