package io.extremum.descriptors.reactive.dao.impl;

import io.extremum.descriptors.common.dao.DescriptorRepository;
import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

public class JpaReactiveDescriptorDao implements ReactiveDescriptorDao {

    public JpaReactiveDescriptorDao(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    private final DescriptorRepository descriptorRepository;

    @Override
    public Mono<Descriptor> retrieveByExternalId(String externalId) {
        return descriptorRepository.findByExternalId(externalId).map(Mono::just).orElse(Mono.empty());
    }

    @Override
    public Mono<Descriptor> retrieveByInternalId(String internalId) {
        return descriptorRepository.findByExternalId(internalId).map(Mono::just).orElse(Mono.empty());
    }

    @Override
    public Mono<Descriptor> retrieveByCollectionCoordinates(String collectionCoordinates) {
        return null;
    }

    @Override
    public Mono<Descriptor> retrieveByOwnedModelCoordinates(String ownedCoordinates) {
        return null;
    }

    @Override
    public Mono<Map<String, String>> retrieveMapByInternalIds(Collection<String> internalIds) {
        return null;
    }

    @Override
    public Mono<Descriptor> retrieveByIri(String iri) {
        return descriptorRepository.findByIri(iri).map(Mono::just).orElse(Mono.empty());
    }

    @Override
    public Flux<Descriptor> retrieveByIriRegex(String pattern) {
        return null;
    }

    @Override
    public Mono<Descriptor> store(Descriptor descriptor) {
        return null;
    }

    @Override
    public Mono<Void> destroy(String externalId) {
        return null;
    }
}
