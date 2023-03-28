package io.extremum.exampleproject.controller;

import io.extremum.sharedmodels.dto.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static io.extremum.sharedmodels.dto.Alert.errorAlert;
import static io.extremum.sharedmodels.dto.Response.fail;

@RestControllerAdvice
public class ExceptionControllerAdvise {

    @ExceptionHandler
    public Response handleEverythingEverythingException(Exception e) {
        e.printStackTrace();
        return fail(errorAlert(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
