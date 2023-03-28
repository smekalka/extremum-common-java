package io.extremum.sku.aop.annotation;

import io.extremum.sku.model.SkuID;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SkuMetric {
    SkuID sku() default SkuID.UNSPECIFIED;

    long amount() default 1;

    boolean returnValue() default false;

    long custom() default -1;

    String channel() default "skuMetricMessageChannel";
}
