package io.extremum.descriptors.common.dao;

import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Optional;

/**
 * @author rpuch
 */
public interface DescriptorRepository {
    Optional<Descriptor> findByExternalId(String externalId);

    Optional<Descriptor> findByInternalId(String internalId);

    Optional<Descriptor> findByIri(String iri);

    Optional<Descriptor> findByCollectionCoordinatesString(String coordinatesString);

    Optional<Descriptor> findByOwnedModelCoordinatesString(String coordinatesString);
}
