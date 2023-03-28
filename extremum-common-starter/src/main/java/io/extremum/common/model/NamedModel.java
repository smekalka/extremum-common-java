package io.extremum.common.model;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.basic.Named;

import java.io.Serializable;

public interface NamedModel<ID extends Serializable> extends BasicModel<ID>, Named {
    ID getId();

    void setId(ID id);

    @Override
    default void copyServiceFieldsTo(Model to) {
        if (!(to instanceof NamedModel)) {
            throw new IllegalStateException("I can only copy to a NamedModel");
        }

        NamedModel<ID> basicTo = (NamedModel<ID>) to;

        basicTo.setId(this.getId());
        basicTo.setUuid(this.getUuid());
        basicTo.setIri(this.getIri());
    }

    enum FIELDS {
        id, uuid, iri
    }
}
