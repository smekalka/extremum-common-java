package io.extremum.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used as a meta-annotation for datasource-altering annotations.
 * If a model class is marked with suh a datasource-altering annotation,
 * this means that repositories created on the 'main' datasource will
 * ignore repositories for the annotated model class.
 *
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface SecondaryDatasource {
}
