package io.extremum.everything.services.collection;

import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.services.FreeCollectionFetcher;
import io.extremum.everything.services.FreeCollectionStreamer;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.CollectionCoordinates;
import io.extremum.sharedmodels.descriptor.FreeCoordinates;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@RequiredArgsConstructor
class FreeCoordinatesHandler implements CoordinatesHandler {
    private final CollectionProviders collectionProviders;

    @Override
    public CollectionFragment<Model> fetchCollection(CollectionCoordinates coordinates, Projection projection) {
        FreeCoordinates freeCoordinates = coordinates.getFreeCoordinates();
        FreeCollectionFetcher<? extends Model> fetcher = collectionProviders.findFreeFetcher(freeCoordinates);
        return fetcher.fetchCollection(freeCoordinates.getParametersString(), projection)
                .map(Function.identity());
    }

    @Override
    public Flux<Model> streamCollection(CollectionCoordinates coordinates, Projection projection) {
        FreeCoordinates freeCoordinates = coordinates.getFreeCoordinates();
        FreeCollectionStreamer<? extends Model> streamer = collectionProviders.findFreeStreamer(freeCoordinates);
        return streamer.streamCollection(freeCoordinates.getParametersString(), projection)
                .map(Function.identity());
    }

}
