package io.extremum.common.exceptions;

import org.springframework.http.HttpStatus;

public class ConverterNotFoundException extends CommonException {
    public ConverterNotFoundException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
