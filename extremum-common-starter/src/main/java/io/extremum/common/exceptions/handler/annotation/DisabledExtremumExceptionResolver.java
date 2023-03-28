package io.extremum.common.exceptions.handler.annotation;

import io.extremum.common.exceptions.ExceptionResponse;
import lombok.SneakyThrows;

public class DisabledExtremumExceptionResolver implements ExtremumExceptionResolver {

    @SneakyThrows
    @Override
    public ExceptionResponse handleException(Throwable throwable) {
        throw throwable;
    }

}
