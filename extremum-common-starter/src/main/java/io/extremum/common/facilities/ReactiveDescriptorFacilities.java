package io.extremum.common.facilities;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactiveDescriptorFacilities {
    Mono<Descriptor> createOrGet(String internalId, String modelType, String iri);

    Mono<Descriptor> fromInternalId(String internalId);

    Mono<String> resolve(Descriptor descriptor);
}
