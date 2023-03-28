package io.extremum.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExtremumRequiredRoles {
    String[] defaultAccess() default {};

    String[] get() default {};

    String[] patch() default {};

    String[] remove() default {};

    String[] watch() default {};
}
