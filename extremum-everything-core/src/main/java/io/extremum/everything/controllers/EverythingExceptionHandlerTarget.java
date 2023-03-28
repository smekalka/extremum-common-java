package io.extremum.everything.controllers;

import io.extremum.everything.aop.DefaultEverythingEverythingExceptionHandler;

import java.lang.annotation.*;

/**
 * Annotation that mark class as target for {@link DefaultEverythingEverythingExceptionHandler}
 *
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface EverythingExceptionHandlerTarget {
}
