package io.extremum.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.Model;

import java.io.Serializable;
import java.time.ZonedDateTime;

public interface PersistableCommonModel<ID extends Serializable> extends BasicModel<ID> {
    ZonedDateTime getCreated();

    void setCreated(ZonedDateTime created);

    ZonedDateTime getModified();

    void setModified(ZonedDateTime modified);

    Long getVersion();

    void setVersion(Long version);

    Boolean getDeleted();

    void setDeleted(Boolean deleted);

    String getCreatedBy();

    void setCreatedBy(String createdBy);

    String getModifiedBy();

    void setModifiedBy(String getModifiedBy);

    @Override
    default void copyServiceFieldsTo(Model to) {
        if (!(to instanceof PersistableCommonModel)) {
            throw new IllegalStateException("I can only copy to a PersistableCommonModel");
        }

        PersistableCommonModel<ID> persistableTo = (PersistableCommonModel<ID>) to;

        BasicModel.super.copyServiceFieldsTo(persistableTo);

        persistableTo.setVersion(this.getVersion());
        persistableTo.setDeleted(this.getDeleted());
        persistableTo.setCreated(this.getCreated());
        persistableTo.setModified(this.getModified());
        persistableTo.setCreatedBy(this.getCreatedBy());
        persistableTo.setModifiedBy(this.getModifiedBy());
    }

    @JsonIgnore
    default boolean isNotDeleted() {
        return getDeleted() == null || !getDeleted();
    }

    enum FIELDS {
        id, uuid, created, modified, version, deleted
    }
}
