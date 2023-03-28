package io.extremum.descriptors.reactive.dao;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

public interface ReactiveDescriptorDao {
    Mono<Descriptor> retrieveByExternalId(String externalId);

    Mono<Descriptor> retrieveByInternalId(String internalId);

    Mono<Descriptor> retrieveByCollectionCoordinates(String collectionCoordinates);

    Mono<Descriptor> retrieveByOwnedModelCoordinates(String ownedCoordinates);

    Mono<Map<String, String>> retrieveMapByInternalIds(Collection<String> internalIds);

    Mono<Descriptor> retrieveByIri(String iri);

    Flux<Descriptor> retrieveByIriRegex(String pattern);

    Mono<Descriptor> store(Descriptor descriptor);

    Mono<Void> destroy(String externalId);
}
