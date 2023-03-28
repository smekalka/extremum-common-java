package io.extremum.common.collection.conversion;

import io.extremum.sharedmodels.fundamental.CollectionReference;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field or a property as a source for a collection that will be exposed
 * via Everything Everything services.
 * Here, 'attribute' means either field or a property.
 * The attribute type must be {@link CollectionReference}.
 * The attribute must be defined in a subclass of {@link ResponseDto}.
 * Infrastructure will generate the ID and URL automatically.
 *
 * This annotation is used to define 'owned' collections.
 * As collection coordinates, they have &lt;host entity descriptor, host attribute name&gt;
 * pair. Host entity descriptor is just a {@link Descriptor} of the {@link ResponseDto}
 * to which the annotated collection instance belongs.
 * Host attribute name is either specified in this annotation or, if omitted, it
 * is considered to be equal to the annotated attribute name.
 * Later, the field/getter corresponding to this attribute will be used to fetch the collection.
 *
 * @author rpuch
 * @see CollectionReference
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface OwnedCollection {
    String hostAttributeName() default "";
}
