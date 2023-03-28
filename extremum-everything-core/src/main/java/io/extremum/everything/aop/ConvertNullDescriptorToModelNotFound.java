package io.extremum.everything.aop;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.common.exceptions.ModelNotFoundException;
import org.springframework.stereotype.Controller;

import java.lang.annotation.*;

/**
 * If a @{@link Controller} annotated with this annotation gets
 * null in any {@link Descriptor} parameter, this will throw
 * a {@link ModelNotFoundException}
 * 
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ConvertNullDescriptorToModelNotFound {
}
