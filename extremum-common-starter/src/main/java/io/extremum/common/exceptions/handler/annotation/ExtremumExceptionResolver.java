package io.extremum.common.exceptions.handler.annotation;

import io.extremum.common.exceptions.ExceptionResponse;

public interface ExtremumExceptionResolver {
    ExceptionResponse handleException(Throwable throwable);
}
