package io.extremum.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate a static method on an Enum which is used to convert
 * from a string representation of an enum constant in some storage
 * to the corresponding Enum instance.
 *
 * Such a method must have the following shape (parameters, return type, static modifier):
 *
 * <pre>
 * &#064;FromStorageString
 * public static MyEnum fromStorageString(String stringValue) {
 *     ...
 * }
 * </pre>
 *
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FromStorageString {
}
