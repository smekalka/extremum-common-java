package io.extremum.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate a non-static method on an Enum which is used to convert
 * from a an enum constant to the corresponding string representation in some storage.
 *
 * Such a method must have the following shape (no parameters, return type, no static modifier):
 *
 * <pre>
 * &#064;ToStorageString
 * public String toStorageString() {
 *     ...
 * }
 * </pre>
 *
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ToStorageString {
}
