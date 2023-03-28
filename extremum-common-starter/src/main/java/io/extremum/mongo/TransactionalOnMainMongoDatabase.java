package io.extremum.mongo;

import io.extremum.mongo.dbfactory.MainMongoDb;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation instead of @{@link Transactional} to mark a method or a type
 * transactional if it uses main Mongo database (aka service Mongo database).
 *
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Transactional(MainMongoDb.QUALIFIER)
public @interface TransactionalOnMainMongoDatabase {
}
