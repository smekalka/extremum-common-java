package io.extremum.common.exceptions;

import io.extremum.sharedmodels.dto.Alert;

import java.util.Collection;

public class UnsupportedOperationException extends CommonException {
    public UnsupportedOperationException(String message, int status) {
        super(message, status);
    }

    public UnsupportedOperationException(String message, int status, Throwable cause) {
        super(message, status, cause);
    }

    public UnsupportedOperationException(Throwable cause, String message, int statusCode) {
        super(cause, message, statusCode);
    }

    public UnsupportedOperationException(String message, int statusCode, Collection<Alert> alerts) {
        super(message, statusCode, alerts);
    }

    public UnsupportedOperationException(int code) {
        super(code);
    }
}
