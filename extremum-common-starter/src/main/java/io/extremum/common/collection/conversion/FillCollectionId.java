package io.extremum.common.collection.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controls whether CollectionReference.id should be filled when url is generated for a CollectionReference.
 * By default, it is filled.
 *
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface FillCollectionId {
    boolean value() default true;
}
