package io.extremum.common.collection.conversion;

import io.extremum.common.attribute.Attribute;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public class CollectionMakeupRequest {
    private final CollectionReference<?> reference;
    @Nullable
    private final Attribute attribute;
    @Nullable
    private final ResponseDto responseDto;
    private final Descriptor collectionDescriptor;

    public CollectionMakeupRequest(CollectionReference<?> reference, Descriptor collectionDescriptor) {
        this(reference, null, null, collectionDescriptor);
    }

    public Optional<Attribute> getAttribute() {
        return Optional.ofNullable(attribute);
    }

    public Optional<ResponseDto> getResponseDto() {
        return Optional.ofNullable(responseDto);
    }
}
