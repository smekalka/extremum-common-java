package io.extremum.everything.services.collection;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;

import java.util.Collection;

/**
 * @author rpuch
 */
interface PagePicker {
    CollectionFragment<Model> getModelsFromModelsCollection(Collection<?> nonEmptyCollection, Projection projection,
            Model host, String hostAttributeName);
}
