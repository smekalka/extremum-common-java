package io.extremum.tx.exceptions;

import io.extremum.common.exceptions.ExceptionResponse;
import io.extremum.common.exceptions.ExtremumExceptionHandlers;
import io.extremum.common.exceptions.handler.annotation.ExtremumExceptionHandler;
import org.springframework.http.HttpStatus;

public class TransactionExceptionHandlers implements ExtremumExceptionHandlers {

    @ExtremumExceptionHandler
    public ExceptionResponse handleAny(TransactionException e) {
        return ExceptionResponse.withMessageAndHttpStatus(e.getMessage(), HttpStatus.valueOf(e.getCode()));
    }
}
