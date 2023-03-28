package io.extremum.mongo.dao.impl;

import io.extremum.common.model.VersionedModel;
import io.extremum.mongo.SoftDeletion;
import org.springframework.data.mongodb.core.query.Criteria;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class VersionedQueries {
    private final SoftDeletion softDeletion = new SoftDeletion();

    public Criteria actualSnapshot() {
        return new Criteria().andOperator(
                softDeletion.notDeleted(),
                where(VersionedModel.FIELDS.currentSnapshot.name()).is(true)
        );
    }
}
