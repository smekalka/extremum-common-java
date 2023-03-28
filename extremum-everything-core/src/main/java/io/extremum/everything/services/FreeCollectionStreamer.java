package io.extremum.everything.services;

import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.basic.Model;
import reactor.core.publisher.Flux;

/**
 * @author rpuch
 */
public interface FreeCollectionStreamer<E extends Model> {
    String getCollectionName();

    Flux<E> streamCollection(String parametersString, Projection projection);
}
