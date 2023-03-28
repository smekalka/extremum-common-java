package io.extremum.sharedmodels.descriptor;

import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * @author rpuch
 */
public interface DescriptorLoader {
    Optional<Descriptor> loadByExternalId(String externalId);

    Optional<Descriptor> loadByInternalId(String internalId);

    Mono<Descriptor> loadByExternalIdReactively(String externalId);

    Mono<Descriptor> loadByInternalIdReactively(String internalId);
}
