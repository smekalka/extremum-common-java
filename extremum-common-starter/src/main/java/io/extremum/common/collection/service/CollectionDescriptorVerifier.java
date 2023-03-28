package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.Descriptor;

class CollectionDescriptorVerifier {
    void makeSureDescriptorContainsCollection(String externalId, Descriptor descriptor) {
        if (!descriptor.isCollection()) {
            throw new IllegalStateException(
                    String.format("Descriptor '%s' must have type COLLECTION, but it is '%s'",
                            externalId, descriptor.getType()));
        }
        if (descriptor.getCollection() == null) {
            throw new IllegalStateException(
                    String.format("Descriptor '%s' has type COLLECTION, but there is no collection in it",
                            externalId));
        }
    }
}
