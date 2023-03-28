package io.extremum.common.exceptions;

import io.extremum.common.exceptions.handler.annotation.ExtremumExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;

import java.util.Optional;

public class DefaultExtremumExceptionHandlers implements ExtremumExceptionHandlers {

    @Order
    @ExtremumExceptionHandler
    public ExceptionResponse handle(CommonException e) {
        HttpStatus httpStatus = resolveHttpStatus(e.getCode()).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        return ExceptionResponse.withMessageAndHttpStatus(e.getMessage(), httpStatus);
    }

    private static Optional<HttpStatus> resolveHttpStatus(int statusCode) {
        return Optional.ofNullable(HttpStatus.resolve(statusCode));
    }

}
