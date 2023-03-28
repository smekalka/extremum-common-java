package io.extremum.descriptors.sync.dao.impl;

import io.extremum.descriptors.common.dao.DescriptorRepository;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JpaDescriptorRepository extends DescriptorRepository, JpaRepository<Descriptor, UUID> {

    @Query(value = "SELECT d FROM Descriptor d WHERE d.collection.coordinatesString = ?1")
    Optional<Descriptor> findByCollectionCoordinatesString(String var1);

    @Query(value = "SELECT d FROM Descriptor d WHERE d.owned.coordinates.ownedCoordinates.coordinatesString = ?1")
    default Optional<Descriptor> findByOwnedModelCoordinatesString(String coordinatesString){
        throw new NotImplementedException();
    }
}