package io.extremum.common.descriptor.service;

import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import io.extremum.sharedmodels.descriptor.DescriptorNotReadyException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyMap;

@RequiredArgsConstructor
public class ReactiveDescriptorServiceImpl implements ReactiveDescriptorService {
    private final ReactiveDescriptorDao reactiveDescriptorDao;

    private final DescriptorReadinessValidation descriptorReadinessValidation = new DescriptorReadinessValidation();

    @Override
    public Mono<Descriptor> store(Descriptor descriptor) {
        Objects.requireNonNull(descriptor, "Descriptor is null");

        return reactiveDescriptorDao.store(descriptor);
    }

    @Override
    public Mono<Descriptor> loadByExternalId(String externalId) {
        Objects.requireNonNull(externalId, "externalId is null");

        return reactiveDescriptorDao.retrieveByExternalId(externalId)
                .map(descriptor -> {
                    if (descriptor.getReadiness() == Descriptor.Readiness.BLANK) {
                        throw new DescriptorNotReadyException(
                                String.format("Descriptor with external ID '%s' is not ready yet", externalId));
                    }
                    return descriptor;
                });
    }

    @Override
    public Mono<Descriptor> loadByIri(String iri) {
        return reactiveDescriptorDao.retrieveByIri(iri)
                .map(descriptor -> {
            if (descriptor.getReadiness() == Descriptor.Readiness.BLANK) {
                throw new DescriptorNotReadyException(
                        String.format("Descriptor with IRI '%s' is not ready yet", iri));
            }
            return descriptor;
        });
    }


    @Override
    public Mono<Descriptor> loadByInternalId(String internalId) {
        Objects.requireNonNull(internalId, "internalId is null");

        return reactiveDescriptorDao.retrieveByInternalId(internalId);
    }

    @Override
    public Mono<Map<String, String>> loadMapByInternalIds(Collection<String> internalIds) {
        if (internalIds.isEmpty()) {
            return Mono.just(emptyMap());
        }

        List<String> internalIdsCopy = new ArrayList<>(internalIds);
        return reactiveDescriptorDao.retrieveMapByInternalIds(internalIdsCopy);
    }

    @Override
    public Mono<Descriptor> makeDescriptorReady(String descriptorExternalId, String modelType) {
        return reactiveDescriptorDao.retrieveByExternalId(descriptorExternalId)
                .doOnNext(descriptor -> descriptorReadinessValidation.validateDescriptorIsNotReady(
                        descriptorExternalId, descriptor))
                .doOnNext(descriptor -> {
                    descriptor.setReadiness(Descriptor.Readiness.READY);
                    descriptor.setModelType(modelType);
                })
                .flatMap(reactiveDescriptorDao::store);
    }

    @Override
    public Mono<Void> destroyDescriptor(String externalId) {
        return reactiveDescriptorDao.destroy(externalId);
    }
}
