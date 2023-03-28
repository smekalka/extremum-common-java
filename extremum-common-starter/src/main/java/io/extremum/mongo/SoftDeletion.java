package io.extremum.mongo;

import io.extremum.common.model.PersistableCommonModel;
import org.springframework.data.mongodb.core.query.Criteria;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author rpuch
 */
public class SoftDeletion {
    private static final String DELETED = PersistableCommonModel.FIELDS.deleted.name();

    public Criteria notDeleted() {
        return where(DELETED).is(false);
    }
}
