package io.extremum.common.model;

import io.extremum.sharedmodels.basic.Model;

import java.io.Serializable;
import java.time.ZonedDateTime;

public interface VersionedModel<ID extends Serializable> extends PersistableCommonModel<ID> {
    ZonedDateTime getStart();

    void setStart(ZonedDateTime modified);

    ZonedDateTime getEnd();

    void setEnd(ZonedDateTime modified);

    @Override
    default ZonedDateTime getModified() {
        return getStart();
    }

    @Override
    default void setModified(ZonedDateTime modified) {
        // doing nothing
    }

    @Override
    default void copyServiceFieldsTo(Model to) {
        if (!(to instanceof VersionedModel)) {
            throw new IllegalStateException("I can only copy to a VersionedModel");
        }

        VersionedModel<ID> persistableTo = (VersionedModel<ID>) to;

        PersistableCommonModel.super.copyServiceFieldsTo(persistableTo);

        persistableTo.setStart(this.getStart());
        persistableTo.setEnd(this.getEnd());
    }

    enum FIELDS {
        id, uuid, lineageId, created, start, end, currentSnapshot, version, deleted
    }
}
