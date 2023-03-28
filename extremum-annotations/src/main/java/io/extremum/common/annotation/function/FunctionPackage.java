package io.extremum.common.annotation.function;

import io.extremum.common.annotation.Localized;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FunctionPackage {
    String name();

    Localized[] description() default {};
}