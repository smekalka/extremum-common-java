package io.extremum.common.descriptor.factory;

import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.common.collection.service.ReactiveOwnedModelDescriptorService;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedModelDescriptor;
import io.extremum.sharedmodels.descriptor.StorageType;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;

public class ReactiveDescriptorSaver {
    private final ReactiveDescriptorService reactiveDescriptorService;
    private final ReactiveCollectionDescriptorService reactiveCollectionDescriptorService;
    private final ReactiveOwnedModelDescriptorService reactiveOwnedModelDescriptorService;

    private final DescriptorSavers savers;

    public ReactiveDescriptorSaver(DescriptorService descriptorService,
                                   ReactiveDescriptorService reactiveDescriptorService,
                                   ReactiveCollectionDescriptorService reactiveCollectionDescriptorService,
                                   ReactiveOwnedModelDescriptorService reactiveOwnedModelDescriptorService) {
        this.reactiveDescriptorService = reactiveDescriptorService;
        savers = new DescriptorSavers(descriptorService);
        this.reactiveCollectionDescriptorService = reactiveCollectionDescriptorService;
        this.reactiveOwnedModelDescriptorService = reactiveOwnedModelDescriptorService;
    }

    public Mono<Descriptor> createAndSave(String internalId, String modelType, StorageType storageType, String iri) {
        Descriptor descriptor = savers.createSingleDescriptor(internalId, modelType, storageType, iri);

        // Loading first to avoid a DuplicateKeyException which aborts a transaction.
        // This is needed to be able to use descriptors under transaction.
        return reactiveDescriptorService.loadByInternalId(descriptor.getInternalId())
                .switchIfEmpty(reactiveDescriptorService.store(descriptor))
                .onErrorResume(DuplicateKeyException.class,
                        ex -> reactiveDescriptorService.loadByInternalId(descriptor.getInternalId()))
                .switchIfEmpty(Mono.defer(() -> Mono.error(
                        new IllegalStateException(String.format("Could not insert nor retrieve by internal ID: " +
                                        "something is wrong! External ID is %s, internal ID is %s",
                                descriptor.getExternalId(), descriptor.getInternalId()))
                        ))
                );
    }

    public Mono<Descriptor> createAndSave(CollectionDescriptor collectionDescriptor) {
        // Loading first to avoid a DuplicateKeyException which aborts a transaction.
        // This is needed to be able to use descriptors under transaction.
        return reactiveCollectionDescriptorService.retrieveByCoordinates(collectionDescriptor)
                .switchIfEmpty(Mono.defer(() -> {
                    Descriptor descriptor = savers.createCollectionDescriptor(collectionDescriptor);
                    return reactiveDescriptorService.store(descriptor);
                }))
                .onErrorResume(DuplicateKeyException.class,
                        ex -> reactiveCollectionDescriptorService.retrieveByCoordinates(collectionDescriptor))
                .switchIfEmpty(Mono.defer(() -> Mono.error(
                        new IllegalStateException(
                                "Could not insert nor retrieve by collection coordinates: something is wrong!")
                )));
    }

    public Mono<Descriptor> createAndSave(OwnedModelDescriptor ownedModelDescriptor) {
        // Loading first to avoid a DuplicateKeyException which aborts a transaction.
        // This is needed to be able to use descriptors under transaction.
        return reactiveOwnedModelDescriptorService.retrieveByCoordinates(ownedModelDescriptor)
                .switchIfEmpty(Mono.defer(() -> {
                    Descriptor descriptor = savers.createOwnedModelDescriptor(ownedModelDescriptor);
                    return reactiveDescriptorService.store(descriptor);
                }))
                .onErrorResume(DuplicateKeyException.class,
                        ex -> reactiveOwnedModelDescriptorService.retrieveByCoordinates(ownedModelDescriptor))
                .switchIfEmpty(Mono.defer(() -> Mono.error(
                        new IllegalStateException(
                                "Could not insert nor retrieve by collection coordinates: something is wrong!")
                )));
    }
}