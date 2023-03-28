package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StorageType;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class BlankDescriptorSaver {

   private static final String  EMPTY_IRI = "";
    private final DescriptorService descriptorService;

    private final DescriptorSavers savers;

    public BlankDescriptorSaver(DescriptorService descriptorService) {
        this.descriptorService = descriptorService;

        savers = new DescriptorSavers(descriptorService);
    }

    public List<Descriptor> createAndSaveBatchOfBlankDescriptors(List<String> internalIds, StorageType storageType) {
        List<Descriptor> descriptors = internalIds.stream()
                .map(internalId -> savers.createSingleDescriptor(internalId, storageType, EMPTY_IRI))
                .peek(descriptor -> descriptor.setReadiness(Descriptor.Readiness.BLANK))
                .collect(toList());

        return descriptorService.storeBatch(descriptors);
    }
}
