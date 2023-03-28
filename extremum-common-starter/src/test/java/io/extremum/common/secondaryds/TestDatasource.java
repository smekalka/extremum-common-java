package io.extremum.common.secondaryds;

import io.extremum.common.annotation.SecondaryDatasource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SecondaryDatasource
public @interface TestDatasource {}
