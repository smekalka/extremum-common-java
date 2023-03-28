package io.extremum.elasticsearch.facilities;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.impl.DescriptorIdResolver;
import io.extremum.common.descriptor.factory.impl.UUIDDescriptorFacilities;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.descriptor.StorageType;

public final class ElasticsearchDescriptorFacilitiesImpl extends UUIDDescriptorFacilities
        implements ElasticsearchDescriptorFacilities {
    public ElasticsearchDescriptorFacilitiesImpl(DescriptorFactory descriptorFactory, DescriptorSaver descriptorSaver,
            DescriptorIdResolver descriptorIdResolver) {
        super(descriptorFactory, descriptorSaver, descriptorIdResolver);
    }

    @Override
    protected StorageType storageType() {
        return StandardStorageType.ELASTICSEARCH;
    }
}