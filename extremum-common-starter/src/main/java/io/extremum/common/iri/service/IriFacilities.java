package io.extremum.common.iri.service;

import io.extremum.common.iri.factory.IriFactory;

public interface IriFacilities {

    IriFactory getIriFactory(Class<?> clazz);

    void register(Class<?> clazz, IriFactory candidate);
}