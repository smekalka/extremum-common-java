package io.extremum.common.collection.conversion;

import io.extremum.common.attribute.Attribute;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
class OwnedCollectionReferenceBrush implements MakeupBrush {
    private final ResponseDto dto;
    private final Attribute attribute;

    @Override
    public CollectionDescriptor collectionDescriptor() {
        return CollectionDescriptor.forOwned(dto.getId(), getHostAttributeName(attribute));
    }

    private String getHostAttributeName(Attribute attribute) {
        OwnedCollection annotation = attribute.getAnnotation(OwnedCollection.class);
        if (StringUtils.isNotBlank(annotation.hostAttributeName())) {
            return annotation.hostAttributeName();
        }
        return attribute.name();
    }

    @Override
    public boolean shouldFillCollectionId() {
        FillCollectionId annotation = attribute.getAnnotation(FillCollectionId.class);
        return annotation == null || annotation.value();
    }

    @Override
    public CollectionMakeupRequest createMakeupRequest(CollectionReference<?> reference,
                                                       Descriptor collectionDescriptor) {
        return new CollectionMakeupRequest(reference, attribute, dto, collectionDescriptor);
    }
}
