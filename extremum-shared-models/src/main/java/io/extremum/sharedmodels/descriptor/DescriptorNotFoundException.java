package io.extremum.sharedmodels.descriptor;

import lombok.Getter;

public class DescriptorNotFoundException extends RuntimeException {

    @Getter
    private Descriptor descriptor;

    public DescriptorNotFoundException(String message) {
        super(message);
    }

    public DescriptorNotFoundException(String message, Descriptor descriptor) {
        super(message);
        this.descriptor = descriptor;
    }
}
