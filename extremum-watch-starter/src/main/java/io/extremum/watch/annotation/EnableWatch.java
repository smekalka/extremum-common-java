package io.extremum.watch.annotation;

import io.extremum.watch.config.WatchConfigurationSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(WatchConfigurationSelector.class)
public @interface EnableWatch {
    boolean reactive() default false;
}
