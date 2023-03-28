package io.extremum.descriptors.sync.dao.impl;

import io.extremum.descriptors.common.dao.DescriptorRepository;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedModelDescriptor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@RequiredArgsConstructor
public class SpringDataDescriptorRepository implements DescriptorRepository {
    private final MongoOperations descriptorMongoOperations;

    @Override
    public Optional<Descriptor> findByExternalId(String externalId) {
        CriteriaDefinition criteria = new Criteria().andOperator(
                where(Descriptor.FIELDS.externalId.name()).is(externalId),
                where(Descriptor.FIELDS.deleted.name()).is(false)
        );
        Descriptor descriptor = descriptorMongoOperations.findOne(new Query(criteria), Descriptor.class);
        return Optional.ofNullable(descriptor);
    }

    @Override
    public Optional<Descriptor> findByInternalId(String internalId) {
        CriteriaDefinition criteria = new Criteria().andOperator(
                where(Descriptor.FIELDS.internalId.name()).is(internalId),
                where(Descriptor.FIELDS.deleted.name()).is(false)
        );
        Descriptor descriptor = descriptorMongoOperations.findOne(new Query(criteria), Descriptor.class);
        return Optional.ofNullable(descriptor);
    }

    @Override
    public Optional<Descriptor> findByIri(String iri) {
        CriteriaDefinition criteria = new Criteria().andOperator(
                where(Descriptor.FIELDS.iri.name()).is(iri),
                where(Descriptor.FIELDS.deleted.name()).is(false)
        );
        Descriptor descriptor = descriptorMongoOperations.findOne(new Query(criteria), Descriptor.class);
        return Optional.ofNullable(descriptor);
    }

    @Override
    public Optional<Descriptor> findByCollectionCoordinatesString(String coordinatesString) {
        String collectionCoordinatesString = Descriptor.FIELDS.collection.name()
                + "." + CollectionDescriptor.FIELDS.coordinatesString.name();
        CriteriaDefinition criteria = new Criteria().andOperator(
                where(collectionCoordinatesString).is(coordinatesString),
                where(Descriptor.FIELDS.deleted.name()).is(false)
        );
        Descriptor descriptor = descriptorMongoOperations.findOne(new Query(criteria), Descriptor.class);
        return Optional.ofNullable(descriptor);
    }

    @Override
    public Optional<Descriptor> findByOwnedModelCoordinatesString(String coordinatesString) {
        String ownedModelCoordinatesString = Descriptor.FIELDS.owned.name()
                + "." + OwnedModelDescriptor.FIELDS.coordinatesString.name();
        CriteriaDefinition criteria = new Criteria().andOperator(
                where(ownedModelCoordinatesString).is(coordinatesString),
                where(Descriptor.FIELDS.deleted.name()).is(false)
        );
        Descriptor descriptor = descriptorMongoOperations.findOne(new Query(criteria), Descriptor.class);
        return Optional.ofNullable(descriptor);
    }
}
