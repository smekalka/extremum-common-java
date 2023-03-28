package io.extremum.common.collection.service;

import com.google.common.collect.ImmutableList;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

import java.util.List;

public class ReactiveCollectionOverridesWithDescriptorExtractorList implements ReactiveCollectionDescriptorExtractor {
    private final List<ReactiveCollectionOverride> overrides;

    private final CollectionDescriptorVerifier collectionDescriptorVerifier = new CollectionDescriptorVerifier();

    public ReactiveCollectionOverridesWithDescriptorExtractorList(List<ReactiveCollectionOverride> overrides) {
        this.overrides = ImmutableList.copyOf(overrides);
    }

    @Override
    public Mono<Descriptor.Type> typeForGetOperation(Descriptor descriptor) {
        return Mono.defer(() -> calculateType(descriptor));
    }

    private Mono<? extends Descriptor.Type> calculateType(Descriptor descriptor) {
        for (ReactiveCollectionOverride override : overrides) {
            if (override.supports(descriptor)) {
                return override.typeForGetOperation(descriptor);
            }
        }
        return descriptor.effectiveTypeReactively();
    }

    @Override
    public Mono<CollectionDescriptor> extractCollectionFromDescriptor(Descriptor descriptor) {
        return Mono.defer(() -> extractCollection(descriptor));
    }

    private Mono<? extends CollectionDescriptor> extractCollection(Descriptor descriptor) {
        for (ReactiveCollectionOverride override : overrides) {
            if (override.supports(descriptor)) {
                return override.extractCollectionFromDescriptor(descriptor);
            }
        }
        return extractCollectionInTheStandardWay(descriptor);
    }

    private Mono<CollectionDescriptor> extractCollectionInTheStandardWay(Descriptor descriptor) {
        collectionDescriptorVerifier.makeSureDescriptorContainsCollection(descriptor.getExternalId(), descriptor);
        return Mono.just(descriptor.getCollection());
    }
}
