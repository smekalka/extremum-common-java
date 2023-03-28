package io.extremum.common.descriptor.service;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

public interface ReactiveDescriptorService {
    Mono<Descriptor> store(Descriptor descriptor);

    Mono<Descriptor> loadByExternalId(String externalId);

    Mono<Descriptor> loadByIri(String iri);

    Mono<Descriptor> loadByInternalId(String internalId);

    Mono<Map<String, String>> loadMapByInternalIds(Collection<String> internalIds);

    Mono<Descriptor> makeDescriptorReady(String descriptorExternalId, String modelType);

    Mono<Void> destroyDescriptor(String externalId);
}
