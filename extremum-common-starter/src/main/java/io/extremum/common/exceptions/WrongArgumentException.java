package io.extremum.common.exceptions;

public class WrongArgumentException extends CommonException {

    public WrongArgumentException(String message) {
        super(message, 400);
    }
}
