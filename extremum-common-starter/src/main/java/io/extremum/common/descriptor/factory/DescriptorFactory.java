package io.extremum.common.descriptor.factory;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StorageType;
import org.apache.commons.lang3.StringUtils;

public final class DescriptorFactory {
    public Descriptor fromExternalId(String externalId) {
        return externalId != null ? new Descriptor(externalId) : null;
    }

    public Descriptor fromInternalIdOfUnknownType(String internalId) {
        return fromInternalIdOrNull(internalId, null);
    }

    public Descriptor fromInternalIdOrNull(String internalId, StorageType storageType) {
        return StringUtils.isBlank(internalId) ? null :
                Descriptor.builder()
                        .internalId(internalId)
                        .storageType(storageType)
                        .build();
    }

    public Descriptor fromInternalId(String internalId, StorageType storageType) {
        if (StringUtils.isBlank(internalId)) {
            throw new IllegalArgumentException("Empty internal id detected");
        }
        return Descriptor.builder()
                .internalId(internalId)
                .storageType(storageType)
                .build();
    }
}
