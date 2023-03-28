package io.extremum.common.model.owned.model;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class OwnedModel implements BasicModel<String> {

    private String owner;

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void setId(String s) {

    }

    @Override
    public Descriptor getUuid() {
        return null;
    }

    @Override
    public void setUuid(Descriptor uuid) {

    }

    @Override
    public String getIri() {
        return null;
    }

    @Override
    public void setIri(String iri) {

    }
}
