package io.extremum.jpa.facilities;

import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Map;
import java.util.UUID;

/**
 * @author rpuch
 */
public interface PostgresDescriptorFacilities {
    Descriptor create(UUID uuid, String modelType, String iri, Map<String, Object> stump);

    Descriptor save(Descriptor descriptor);

    Descriptor fromInternalId(UUID uuid);

    Descriptor fromInternalIdOrNull(String uuid);

    UUID resolve(Descriptor descriptor);
}
