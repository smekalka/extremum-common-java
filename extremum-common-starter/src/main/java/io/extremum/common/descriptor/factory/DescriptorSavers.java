package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedModelDescriptor;
import io.extremum.sharedmodels.descriptor.StorageType;

public class DescriptorSavers {
    public final static String EXTERNAL_ID_TEMPLATE = "${externalId}";
    private final DescriptorService descriptorService;

    public DescriptorSavers(DescriptorService descriptorService) {
        this.descriptorService = descriptorService;
    }

    public Descriptor createSingleDescriptor(String internalId, StorageType storageType, String iri) {
        return createSingleDescriptor(internalId, null, storageType, iri);
    }

    Descriptor createSingleDescriptor(String internalId, String modelType, StorageType storageType, String iri) {
        String externalId = descriptorService.createExternalId();
        String iriToSave;
        if (iri != null) {
            iriToSave = iri.replace(EXTERNAL_ID_TEMPLATE, externalId);
        } else {
            iriToSave = "/" + externalId;
        }

        return Descriptor.builder()
                .externalId(externalId)
                .iri(iriToSave)
                .type(Descriptor.Type.SINGLE)
                .readiness(Descriptor.Readiness.READY)
                .internalId(internalId)
                .modelType(modelType)
                .storageType(storageType)
                .build();
    }

    public Descriptor createCollectionDescriptor(CollectionDescriptor collectionDescriptor) {
        return Descriptor.forCollection(descriptorService.createExternalId(), collectionDescriptor);
    }

    public Descriptor createOwnedModelDescriptor(OwnedModelDescriptor ownedModelDescriptor) {
        return Descriptor.forOwnedModel(descriptorService.createExternalId(), ownedModelDescriptor);
    }
}
