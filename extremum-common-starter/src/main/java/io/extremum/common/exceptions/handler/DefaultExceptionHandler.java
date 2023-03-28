package io.extremum.common.exceptions.handler;

import io.extremum.common.exceptions.ExceptionResponse;
import io.extremum.common.exceptions.handler.annotation.ExtremumExceptionResolver;
import io.extremum.sharedmodels.dto.Response;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order
public class DefaultExceptionHandler {

    private final ExtremumExceptionResolver extremumExceptionHandler;

    public DefaultExceptionHandler(ExtremumExceptionResolver extremumExceptionHandler) {
        this.extremumExceptionHandler = extremumExceptionHandler;
    }

    @ExceptionHandler
    @Order
    public ResponseEntity<Response> handle(Exception e) {
        ExceptionResponse exceptionResponse = extremumExceptionHandler.handleException(e);
        return ResponseEntity
                .status(exceptionResponse.getHttpStatus())
                .body(exceptionResponse.getData());
    }

}
