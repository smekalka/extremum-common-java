package io.extremum.everything.services.collection;

import io.extremum.everything.services.FreeCollectionFetcher;
import io.extremum.everything.services.FreeCollectionStreamer;
import io.extremum.everything.services.OwnedCollectionFetcher;
import io.extremum.everything.services.OwnedCollectionStreamer;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.FreeCoordinates;
import io.extremum.sharedmodels.descriptor.OwnedCoordinates;

import java.util.Optional;

public interface CollectionProviders {
    Optional<OwnedCollectionFetcher> findOwnedFetcher(OwnedCoordinates owned);

    Optional<OwnedCollectionStreamer> findOwnedStreamer(OwnedCoordinates owned);

    FreeCollectionFetcher<? extends Model> findFreeFetcher(FreeCoordinates freeCoordinates);

    FreeCollectionStreamer<? extends Model> findFreeStreamer(FreeCoordinates freeCoordinates);
}
