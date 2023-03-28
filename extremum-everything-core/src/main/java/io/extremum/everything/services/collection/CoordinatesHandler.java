package io.extremum.everything.services.collection;

import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.CollectionCoordinates;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface CoordinatesHandler {
    CollectionFragment<Model> fetchCollection(CollectionCoordinates coordinates, Projection projection);

    Flux<Model> streamCollection(CollectionCoordinates coordinates, Projection projection);

    default Mono<CollectionFragment<Model>> fetchCollectionReactively(
            CollectionCoordinates coordinates, Projection projection) {
        // TODO: decide how to deal with this. We cannot return all the values without
        // a restriction here. Should we remove GET with Accept: application/json altogether?
        return streamCollection(coordinates, projection)
                .take(projection.getLimit().orElse(10))
                .collectList()
                .map(CollectionFragment::forUnknownSize);
    }

}
