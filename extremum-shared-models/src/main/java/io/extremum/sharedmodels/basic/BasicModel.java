package io.extremum.sharedmodels.basic;

import java.io.Serializable;

public interface BasicModel<ID extends Serializable> extends Model, HasUuid {
    ID getId();

    void setId(ID id);

    @Override
    default void copyServiceFieldsTo(Model to) {
        if (!(to instanceof BasicModel)) {
            throw new IllegalStateException("I can only copy to a BasicModel");
        }

        BasicModel<ID> basicTo = (BasicModel<ID>) to;

        basicTo.setId(this.getId());
        basicTo.setUuid(this.getUuid());
        basicTo.setIri(this.getIri());
    }

    enum FIELDS {
        id, uuid, iri
    }
}
