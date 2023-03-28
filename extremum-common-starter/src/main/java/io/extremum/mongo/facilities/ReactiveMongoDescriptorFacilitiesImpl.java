package io.extremum.mongo.facilities;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.descriptor.factory.impl.ReactiveDescriptorIdResolver;
import io.extremum.facilities.ReactiveDescriptorFacilitiesImpl;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.descriptor.StorageType;

public final class ReactiveMongoDescriptorFacilitiesImpl extends ReactiveDescriptorFacilitiesImpl
        implements ReactiveMongoDescriptorFacilities {

    public ReactiveMongoDescriptorFacilitiesImpl(DescriptorFactory descriptorFactory,
            ReactiveDescriptorSaver descriptorSaver, ReactiveDescriptorIdResolver descriptorIdResolver) {
        super(descriptorFactory, descriptorSaver, descriptorIdResolver);
    }

    @Override
    protected StorageType storageType() {
        return StandardStorageType.MONGO;
    }
}
