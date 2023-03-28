package io.extremum.common.collection.service;

import io.extremum.common.descriptor.factory.DescriptorSavers;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.descriptors.sync.dao.DescriptorDao;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public class CollectionDescriptorServiceImpl implements CollectionDescriptorService {
    private final DescriptorService descriptorService;
    private final DescriptorDao descriptorDao;
    private final DescriptorSavers descriptorSavers;

    private final CollectionDescriptorVerifier collectionDescriptorVerifier = new CollectionDescriptorVerifier();

    public CollectionDescriptorServiceImpl(DescriptorService descriptorService, DescriptorDao descriptorDao) {
        this.descriptorService = descriptorService;
        this.descriptorDao = descriptorDao;
        descriptorSavers = new DescriptorSavers(descriptorService);
    }

    @Override
    public Optional<CollectionDescriptor> retrieveByExternalId(String externalId) {
        Optional<Descriptor> optDescriptor = descriptorService.loadByExternalId(externalId);

        optDescriptor.ifPresent(descriptor -> {
            collectionDescriptorVerifier.makeSureDescriptorContainsCollection(externalId, descriptor);
        });

        return optDescriptor.map(Descriptor::getCollection);
    }

    @Override
    public Descriptor retrieveByCoordinatesOrCreate(CollectionDescriptor collectionDescriptor) {
        return descriptorDao
                .retrieveByCollectionCoordinates(collectionDescriptor.toCoordinatesString())
                .orElseGet(() -> descriptorDao.store(
                        descriptorSavers.createCollectionDescriptor(collectionDescriptor))
                );
    }
}
