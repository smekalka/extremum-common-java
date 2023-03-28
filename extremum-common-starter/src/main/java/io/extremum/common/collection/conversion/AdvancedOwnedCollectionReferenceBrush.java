package io.extremum.common.collection.conversion;

import io.extremum.common.attribute.Attribute;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.apache.commons.lang3.StringUtils;

public class AdvancedOwnedCollectionReferenceBrush extends OwnedCollectionReferenceBrush {

    public AdvancedOwnedCollectionReferenceBrush(ResponseDto dto, Attribute attribute) {
        super(dto, attribute);
        this.attribute = attribute;
        this.dto = dto;
    }

    private final ResponseDto dto;
    private final Attribute attribute;

    @Override
    public CollectionDescriptor collectionDescriptor() {
        return CollectionDescriptor.forOwned(this.dto.getId(), this.getHostAttributeName(this.attribute));
    }

    private String getHostAttributeName(Attribute attribute) {
        OwnedCollection annotation = attribute.getAnnotation(OwnedCollection.class);
        if (annotation != null) {
            return StringUtils.isNotBlank(annotation.hostAttributeName()) ? annotation.hostAttributeName() : attribute.name();
        }

        return attribute.name();
    }
}