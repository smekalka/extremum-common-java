package io.extremum.common.descriptor.service;

import io.extremum.mongo.facilities.DescriptorIsAlreadyReadyException;
import io.extremum.sharedmodels.descriptor.Descriptor;

public class DescriptorReadinessValidation {
    public void validateDescriptorIsNotReady(String descriptorId, Descriptor descriptor) {
        if (descriptor.getReadiness() == Descriptor.Readiness.READY) {
            throw new DescriptorIsAlreadyReadyException(
                    "The descriptor with external ID '" + descriptorId + "' is already ready");
        }
    }
}
