package io.extremum.common.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity as a 'hard-delete' entity. This means that its
 * repository implementation will actually delete records from the storage.
 * The default (when this annotation is not present) is to treat entities
 * as 'soft-delete' for which repositories do not actually delete records,
 * but mark them 'deleted'.
 *
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HardDelete {
}
