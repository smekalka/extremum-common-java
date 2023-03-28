package io.extremum.mongo.dbfactory;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks beans related to the main MongoDB (distinct from Descriptor-related DB, for example).
 *
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Qualifier(MainMongoDb.QUALIFIER)
public @interface MainMongoDb {
    String QUALIFIER = "mainMongoDbQualifier";
}
