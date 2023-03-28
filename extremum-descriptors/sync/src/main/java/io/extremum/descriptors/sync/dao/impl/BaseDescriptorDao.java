package io.extremum.descriptors.sync.dao.impl;

import io.extremum.descriptors.sync.dao.DescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.redisson.api.RMap;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.data.mongodb.core.query.Criteria.where;


public abstract class BaseDescriptorDao implements DescriptorDao {
    private final MongoOperations descriptorMongoOperations;
    private final RMap<String, Descriptor> descriptors;
    private final RMap<String, String> internalIdIndex;
    private final RMap<String, String> iriIndex;
    private final RMap<String, String> collectionCoordinatesToExternalIds;

    BaseDescriptorDao(MongoOperations descriptorMongoOperations,
                      RMap<String, Descriptor> descriptors,
                      RMap<String, String> internalIdIndex,
                      RMap<String, String> collectionCoordinatesToExternalIds,
                      RMap<String, String> iriIndex
    ) {
        this.descriptorMongoOperations = descriptorMongoOperations;
        this.descriptors = descriptors;
        this.internalIdIndex = internalIdIndex;
        this.iriIndex = iriIndex;
        this.collectionCoordinatesToExternalIds = collectionCoordinatesToExternalIds;
    }

    @Override
    public Optional<Descriptor> retrieveByExternalId(String externalId) {
        return Optional.ofNullable(descriptors.get(externalId));
    }

    @Override
    public Optional<Descriptor> retrieveByInternalId(String internalId) {
        String descriptorId = internalIdIndex.get(internalId);

        return Optional.ofNullable(descriptorId).map(descriptors::get);
    }

    @Override
    public Map<String, Descriptor> retrieveDescriptors(Collection<String> externalIds) {
        return descriptors.getAll(new HashSet<>(externalIds));
    }

    @Override
    public Optional<Descriptor> retrieveByCollectionCoordinates(String collectionCoordinates) {
        String descriptorId = collectionCoordinatesToExternalIds.get(collectionCoordinates);

        return Optional.ofNullable(descriptorId).map(descriptors::get);
    }

    @Override
    public Map<String, String> retrieveMapByExternalIds(Collection<String> externalIds) {
        Map<String, Descriptor> all = descriptors.getAll(new HashSet<>(externalIds));

        return all.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().getInternalId()));
    }

    @Override
    public Map<String, String> retrieveMapByInternalIds(Collection<String> internalIds) {
        return internalIdIndex.getAll(new HashSet<>(internalIds));
    }

    @Override
    public Descriptor store(Descriptor descriptor) {
        Descriptor storedInMongo = descriptorMongoOperations.save(descriptor);

        putToMaps(storedInMongo);

        return storedInMongo;
    }

    private void putToMaps(Descriptor descriptor) {
        descriptors.put(descriptor.getExternalId(), descriptor);
        if (descriptor.isSingle()) {
            internalIdIndex.put(descriptor.getInternalId(), descriptor.getExternalId());
            iriIndex.put(descriptor.getIri(), descriptor.getExternalId());
        }
        if (descriptor.isCollection()) {
            collectionCoordinatesToExternalIds.put(
                    descriptor.getCollection().toCoordinatesString(), descriptor.getExternalId());
        }
    }

    @Override
    public List<Descriptor> storeBatch(List<Descriptor> descriptorsToSave) {
        List<Descriptor> storedToMongo = descriptorsToSave.stream()
                .map(descriptorMongoOperations::save)
                .collect(toList());

        try {
            putManyToMaps(storedToMongo);
        } catch (RuntimeException e) {
            try {
                destroyBatch(storedToMongo);
            } catch (Exception e1) {
                e.addSuppressed(e1);
            }
            throw e;
        }

        return storedToMongo;
    }

    private void putManyToMaps(List<Descriptor> descriptorsToSave) {
        Map<String, Descriptor> mapByExternalId = descriptorsToSave.stream()
                .collect(toMap(Descriptor::getExternalId, identity()));
        descriptors.putAll(mapByExternalId);

        Map<String, String> mapByInternalId = descriptorsToSave.stream()
                .filter(Descriptor::isSingle)
                .collect(toMap(Descriptor::getInternalId, Descriptor::getExternalId));
        internalIdIndex.putAll(mapByInternalId);

        Map<String, String> mapByCollectionCoordinates = descriptorsToSave.stream()
                .filter(Descriptor::isCollection)
                .collect(toMap(descriptor ->
                        descriptor.getCollection().toCoordinatesString(), Descriptor::getExternalId));
        collectionCoordinatesToExternalIds.putAll(mapByCollectionCoordinates);
    }

    @Override
    public void destroyBatch(List<Descriptor> descriptorsToDestroy) {
        String[] externalIds = descriptorsToDestroy.stream()
                .map(Descriptor::getExternalId)
                .toArray(String[]::new);

        destroyInMongo(externalIds);

        descriptors.fastRemove(externalIds);

        String[] internalIds = descriptorsToDestroy.stream()
                .filter(Descriptor::isSingle)
                .map(Descriptor::getInternalId)
                .toArray(String[]::new);
        internalIdIndex.fastRemove(internalIds);

        String[] coordinateStrings = descriptorsToDestroy.stream()
                .filter(Descriptor::isCollection)
                .map(descriptor -> descriptor.getCollection().toCoordinatesString())
                .toArray(String[]::new);
        collectionCoordinatesToExternalIds.fastRemove(coordinateStrings);
    }

    private void destroyInMongo(String[] externalIds) {
        Criteria criteria = where("_id").in(Arrays.asList(externalIds));
        descriptorMongoOperations.remove(new Query(criteria), Descriptor.class);
    }
}
