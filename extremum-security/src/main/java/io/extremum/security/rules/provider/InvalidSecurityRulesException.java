package io.extremum.security.rules.provider;

public class InvalidSecurityRulesException extends RuntimeException {
    public InvalidSecurityRulesException(String message, Throwable e) {
        super(message, e);
    }
}
