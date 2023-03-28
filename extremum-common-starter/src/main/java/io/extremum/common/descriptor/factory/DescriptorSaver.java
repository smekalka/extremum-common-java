package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedModelDescriptor;
import io.extremum.sharedmodels.descriptor.StorageType;

import java.util.Map;

public class DescriptorSaver {
    private final DescriptorService descriptorService;

    private final DescriptorSavers savers;

    public DescriptorSaver(DescriptorService descriptorService) {
        this.descriptorService = descriptorService;
        savers = new DescriptorSavers(descriptorService);
    }

    public Descriptor createAndSave(String internalId, String modelType, StorageType storageType, String iri, Map<String, Object> stump) {
        Descriptor descriptor = savers.createSingleDescriptor(internalId, modelType, storageType, iri);
        descriptor.setStump(stump);

        return descriptorService.store(descriptor);
    }

    public Descriptor createAndSave(CollectionDescriptor collectionDescriptor) {
        Descriptor descriptor = Descriptor.forCollection(descriptorService.createExternalId(), collectionDescriptor);
        return descriptorService.store(descriptor);
    }

    public Descriptor createAndSave(OwnedModelDescriptor ownedModelDescriptor) {
        Descriptor descriptor = Descriptor.forOwnedModel(descriptorService.createExternalId(), ownedModelDescriptor);
        return descriptorService.store(descriptor);
    }

    public Descriptor save(Descriptor descriptor) {
        return descriptorService.store(descriptor);
    }
}
