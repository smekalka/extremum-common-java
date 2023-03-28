package io.extremum.everything.services;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;

/**
 * A component that may be used to override the default 'collection fetching'
 * logic. If it is present in the application context and found by
 * getSupportedModel() + getHostAttributeName(), it will be used to search for
 * the collection chunk instead of the default method.
 *
 * @author rpuch
 */
public interface OwnedCollectionFetcher<H extends BasicModel, E extends Model>
        extends EverythingEverythingService {
    /**
     * Returns the attribute (field/property) name of the host object to which this collection
     * is mapped.
     *
     * @return host property name
     */
    String getHostAttributeName();

    CollectionFragment<E> fetchCollection(H host, Projection projection);
}
