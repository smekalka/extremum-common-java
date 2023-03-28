package io.extremum.security;

/**
 * @author rpuch
 */
public class ExtremumAccessDeniedException extends ExtremumSecurityException {
    public ExtremumAccessDeniedException(String message) {
        super(message);
    }
}
