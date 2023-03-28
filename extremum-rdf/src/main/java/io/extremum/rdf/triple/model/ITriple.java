package io.extremum.rdf.triple.model;

import io.extremum.sharedmodels.basic.Model;

import java.util.Collection;

public interface ITriple extends Model {
    String getSubject();

    String getPredicate();

    Collection<String> getObjects();
}
