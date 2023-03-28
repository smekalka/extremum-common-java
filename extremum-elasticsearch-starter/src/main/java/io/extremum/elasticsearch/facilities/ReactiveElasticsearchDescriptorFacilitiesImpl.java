package io.extremum.elasticsearch.facilities;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.descriptor.factory.impl.ReactiveDescriptorIdResolver;
import io.extremum.facilities.ReactiveDescriptorFacilitiesImpl;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.descriptor.StorageType;

public final class ReactiveElasticsearchDescriptorFacilitiesImpl extends ReactiveDescriptorFacilitiesImpl
        implements ReactiveElasticsearchDescriptorFacilities {
    public ReactiveElasticsearchDescriptorFacilitiesImpl(DescriptorFactory descriptorFactory,
            ReactiveDescriptorSaver descriptorSaver, ReactiveDescriptorIdResolver descriptorIdResolver) {
        super(descriptorFactory, descriptorSaver, descriptorIdResolver);
    }

    @Override
    protected StorageType storageType() {
        return StandardStorageType.ELASTICSEARCH;
    }
}