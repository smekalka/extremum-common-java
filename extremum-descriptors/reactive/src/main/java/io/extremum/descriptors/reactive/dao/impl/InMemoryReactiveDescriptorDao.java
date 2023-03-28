package io.extremum.descriptors.reactive.dao.impl;

import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public class InMemoryReactiveDescriptorDao implements ReactiveDescriptorDao {

    private final ConcurrentMap<String, Descriptor> externalIdToDescriptorMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> internalIdToExternalIdMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> internalIdToIriMap = new ConcurrentHashMap<>();


    @Override
    public Mono<Descriptor> retrieveByExternalId(String externalId) {
        return Mono.fromSupplier(() -> externalIdToDescriptorMap.get(externalId));
    }

    @Override
    public Mono<Descriptor> retrieveByInternalId(String internalId) {
        return Mono.fromSupplier(() -> internalIdToExternalIdMap.get(internalId))
                .flatMap(this::retrieveByExternalId);
    }

    @Override
    public Mono<Descriptor> retrieveByCollectionCoordinates(String collectionCoordinates) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Mono<Descriptor> retrieveByOwnedModelCoordinates(String ownedCoordinates) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Mono<Map<String, String>> retrieveMapByInternalIds(Collection<String> internalIds) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Mono<Descriptor> retrieveByIri(String iri) {
        return Mono.fromSupplier(() -> internalIdToExternalIdMap.get(iri))
                .flatMap(this::retrieveByExternalId);
    }

    @Override
    public Flux<Descriptor> retrieveByIriRegex(String pattern) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Mono<Descriptor> store(Descriptor descriptor) {
        externalIdToDescriptorMap.put(descriptor.getExternalId(), descriptor);
        internalIdToExternalIdMap.put(descriptor.getInternalId(), descriptor.getInternalId());

        return Mono.just(descriptor);
    }

    @Override
    public Mono<Void> destroy(String externalId) {
        return null;
    }

    public Stream<Descriptor> descriptors() {
        return externalIdToDescriptorMap.values().stream();
    }
}
