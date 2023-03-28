package io.extremum.common.exceptions;

/**
 * Used for the cases when an exception can only mean that we (the programmers)
 * did something wrong. This is a programming error and should be fixed in the code.
 * This is not something caused by a network/device/weather failure.
 *
 * @author rpuch
 */
public class ProgrammingErrorException extends RuntimeException {
    public ProgrammingErrorException(String message) {
        super(message);
    }

    public ProgrammingErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
