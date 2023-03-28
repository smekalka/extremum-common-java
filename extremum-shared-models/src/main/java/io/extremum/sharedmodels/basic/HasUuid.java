package io.extremum.sharedmodels.basic;

import io.extremum.sharedmodels.descriptor.Descriptor;

public interface HasUuid {
    Descriptor getUuid();

    void setUuid(Descriptor uuid);

    String getIri();

    void setIri(String iri);
}