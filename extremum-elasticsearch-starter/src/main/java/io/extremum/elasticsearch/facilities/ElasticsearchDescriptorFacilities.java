package io.extremum.elasticsearch.facilities;

import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.UUID;

/**
 * @author rpuch
 */
public interface ElasticsearchDescriptorFacilities {
    Descriptor create(UUID uuid, String modelType, String iri);

    Descriptor fromInternalId(UUID uuid);

    Descriptor fromInternalId(String internalId);

    Descriptor fromInternalIdOrNull(String uuid);

    UUID resolve(Descriptor descriptor);
}
