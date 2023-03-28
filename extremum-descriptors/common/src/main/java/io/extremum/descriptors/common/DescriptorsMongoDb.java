package io.extremum.descriptors.common;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks beans related to the descriptor-related MongoDB (distinct from the main DB, for example).
 *
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Qualifier(DescriptorsMongoDb.QUALIFIER)
public @interface DescriptorsMongoDb {
    String QUALIFIER = "descriptorsMongoDbQualifier";
}
