package io.extremum.common.collection.conversion;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class PreExistingCollectionReferenceBrush implements MakeupBrush {
    private final CollectionDescriptor collectionDescriptor;

    @Override
    public CollectionDescriptor collectionDescriptor() {
        return collectionDescriptor;
    }

    @Override
    public boolean shouldFillCollectionId() {
        return true;
    }

    @Override
    public CollectionMakeupRequest createMakeupRequest(CollectionReference<?> reference,
                                                       Descriptor collectionDescriptor) {
        return new CollectionMakeupRequest(reference, collectionDescriptor);
    }
}
