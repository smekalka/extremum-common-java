package io.extremum.sharedmodels.descriptor;

public class DescriptorNotReadyException extends RuntimeException {
    public DescriptorNotReadyException(String message) {
        super(message);
    }
}
