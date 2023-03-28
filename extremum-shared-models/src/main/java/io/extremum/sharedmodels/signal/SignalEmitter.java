package io.extremum.sharedmodels.signal;

import io.extremum.sharedmodels.watch.ModelSignalType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SignalEmitter {
    ModelSignalType[] value() default {};
}