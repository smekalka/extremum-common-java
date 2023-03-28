package io.extremum.jpa.facilities;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.impl.DescriptorIdResolver;
import io.extremum.common.descriptor.factory.impl.UUIDDescriptorFacilities;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.descriptor.StorageType;

import java.util.UUID;

public final class PostgresDescriptorFacilitiesImpl extends UUIDDescriptorFacilities
        implements PostgresDescriptorFacilities {
    public PostgresDescriptorFacilitiesImpl(DescriptorFactory descriptorFactory, DescriptorSaver descriptorSaver,
            DescriptorIdResolver descriptorIdResolver) {
        super(descriptorFactory, descriptorSaver, descriptorIdResolver);
    }

    @Override
    protected StorageType storageType() {
        return StandardStorageType.POSTGRES;
    }
}
