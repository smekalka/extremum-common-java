package io.extremum.sharedmodels.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * May be used to annotate classes that use implicit dependencies
 * violating 'Dependency Injection' principle and obtained statically
 * (for example, via a static accessor).
 *
 * @author rpuch
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface UsesStaticDependencies {
}
