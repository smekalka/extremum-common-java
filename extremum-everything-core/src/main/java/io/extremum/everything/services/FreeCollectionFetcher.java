package io.extremum.everything.services;

import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.basic.Model;

/**
 * @author rpuch
 */
public interface FreeCollectionFetcher<E extends Model> {
    String getCollectionName();

    CollectionFragment<E> fetchCollection(String parametersString, Projection projection);
}
