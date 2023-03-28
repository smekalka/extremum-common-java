package io.extremum.common.collection.conversion;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.fundamental.CollectionReference;

public interface MakeupBrush {
    CollectionDescriptor collectionDescriptor();

    boolean shouldFillCollectionId();

    CollectionMakeupRequest createMakeupRequest(CollectionReference<?> reference, Descriptor collectionDescriptor);
}
