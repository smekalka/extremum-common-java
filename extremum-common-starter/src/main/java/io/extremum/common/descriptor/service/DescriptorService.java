package io.extremum.common.descriptor.service;

import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service to work with {@link Descriptor}s.
 *
 * Please note that this service does not tolerate null arguments by default. If a null argument
 * is passed, a {@link NullPointerException} is thrown.
 */
public interface DescriptorService {

    String createExternalId();

    Descriptor store(Descriptor descriptor);

    List<Descriptor> storeBatch(List<Descriptor> descriptors);

    /**
     * Loads an optional {@link Descriptor} by its external ID.
     *
     * @param externalId external ID to search by; cannot be null
     * @return an optional {@link Descriptor}
     * @throws NullPointerException if externalId is null
     */
    Optional<Descriptor> loadByExternalId(String externalId);

    /**
     * Loads an optional {@link Descriptor} by its internal ID.
     *
     * @param internalId internal ID to search by; cannot be null
     * @return an optional {@link Descriptor}
     * @throws NullPointerException if internalId is null
     */
    Optional<Descriptor> loadByInternalId(String internalId);

    Map<String, String> loadMapByExternalIds(Collection<String> externalIds);

    Map<String, Descriptor> loadDescriptorsByExternalIds(Collection<String> externalIds);

    Map<String, String> loadMapByInternalIds(Collection<String> internalIds);

    Descriptor makeDescriptorReady(String descriptorExternalId, String modelType);
}
