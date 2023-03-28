package io.extremum.common.iri.factory;

import io.extremum.sharedmodels.basic.BasicModel;

public interface IriFactory {

    String create(BasicModel<?> nested, BasicModel<?> folder);

    String create(BasicModel<?> nested);
}
