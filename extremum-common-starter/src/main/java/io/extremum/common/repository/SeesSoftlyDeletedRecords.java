package io.extremum.common.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to turn the 'soft deletion respecting' query logic off
 * for an annotated method.
 *
 * All our repositories are using 'soft deletion' policy by default. This means that
 * we usually do not delete anything; instead, we just set 'deleted' flag to true.
 * For this reason, Spring Data magic queries (like <code>Person findByEmail(String email)</code>)
 * produce database queries that include 'and not deleted' predicate by default.
 *
 * If a query method for some reason needs to see these deleted records, put this annotation
 * on it. It will disable the default 'and not deleted' predicate addition.
 *
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SeesSoftlyDeletedRecords {
}
