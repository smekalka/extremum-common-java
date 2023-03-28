package io.extremum.everything.services;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.basic.Model;
import reactor.core.publisher.Flux;

/**
 * A component that may be used to override the default 'owned collection streaming'
 * logic. If it is present in the application context and found by
 * getSupportedModel() + getHostAttributeName(), it will be used to stream
 * the collection chunk instead of the default method.
 *
 * @author rpuch
 */
public interface OwnedCollectionStreamer<H extends BasicModel, E extends Model>
        extends EverythingEverythingService {
    /**
     * Returns the attribute (field/property) name of the host object to which this collection
     * is mapped.
     *
     * @return host property name
     */
    String getHostAttributeName();

    Flux<E> streamCollection(H host, Projection projection);
}
