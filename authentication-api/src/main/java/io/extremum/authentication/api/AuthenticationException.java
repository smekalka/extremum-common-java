package io.extremum.authentication.api;

/**
 * @author rpuch
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
