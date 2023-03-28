package io.extremum.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotations marked with this annotation should not be used by application developers.
 * They are part of the infrastructure.
 *
 * @author rpuch
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
public @interface InfrastructureElement {
}
