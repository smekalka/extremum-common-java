package io.extremum.common.collection.conversion;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ReferenceContext {
    @Getter
    private final CollectionReference<?> reference;
    private final MakeupBrush brush;

    CollectionDescriptor collectionDescriptor() {
        return brush.collectionDescriptor();
    }

    void fillReferenceId(String externalCollectionId) {
        if (reference.getId() == null && brush.shouldFillCollectionId()) {
            reference.setId(externalCollectionId);
        }
    }

    CollectionMakeupRequest createMakeupRequest(Descriptor collectionDescriptor) {
        return brush.createMakeupRequest(reference, collectionDescriptor);
    }
}
