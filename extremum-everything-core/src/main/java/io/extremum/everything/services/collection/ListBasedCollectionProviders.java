package io.extremum.everything.services.collection;

import io.extremum.everything.services.FreeCollectionFetcher;
import io.extremum.everything.services.FreeCollectionStreamer;
import io.extremum.everything.services.OwnedCollectionFetcher;
import io.extremum.everything.services.OwnedCollectionStreamer;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.FreeCoordinates;
import io.extremum.sharedmodels.descriptor.OwnedCoordinates;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class ListBasedCollectionProviders implements CollectionProviders {
    private final List<OwnedCollectionFetcher> ownedCollectionFetchers;
    private final List<OwnedCollectionStreamer> ownedCollectionStreamers;
    private final List<FreeCollectionFetcher<? extends Model>> freeCollectionFetchers;
    private final List<FreeCollectionStreamer<? extends Model>> freeCollectionStreamers;

    @Override
    public Optional<OwnedCollectionFetcher> findOwnedFetcher(OwnedCoordinates owned) {
        return ownedCollectionFetchers.stream()
                .filter(fetcher -> fetcher.getSupportedModel().equals(owned.getHostId().getModelType()))
                .filter(fetcher -> fetcher.getHostAttributeName().equals(owned.getHostAttributeName()))
                .findFirst();
    }

    @Override
    public Optional<OwnedCollectionStreamer> findOwnedStreamer(OwnedCoordinates owned) {
        return ownedCollectionStreamers.stream()
                .filter(streamer -> streamer.getSupportedModel().equals(owned.getHostId().getModelType()))
                .filter(streamer -> streamer.getHostAttributeName().equals(owned.getHostAttributeName()))
                .findFirst();
    }

    @Override
    public FreeCollectionFetcher<? extends Model> findFreeFetcher(FreeCoordinates freeCoordinates) {
        String freeCollectionName = freeCoordinates.getName();
        return freeCollectionFetchers.stream()
                .filter(fetcher -> fetcherSupportsName(fetcher, freeCollectionName))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Did not find a free collection fetcher supporting name '%s'",
                                freeCollectionName)));
    }

    private boolean fetcherSupportsName(FreeCollectionFetcher<?> fetcher, String freeCollectionName) {
        return Objects.equals(fetcher.getCollectionName(), freeCollectionName);
    }

    @Override
    public FreeCollectionStreamer<? extends Model> findFreeStreamer(FreeCoordinates freeCoordinates) {
        String freeCollectionName = freeCoordinates.getName();
        return freeCollectionStreamers.stream()
                .filter(streamer -> streamerSupportsName(streamer, freeCollectionName))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Did not find a free collection streamer supporting name '%s'",
                                freeCollectionName)));
    }

    private boolean streamerSupportsName(FreeCollectionStreamer<?> streamer, String freeCollectionName) {
        return Objects.equals(streamer.getCollectionName(), freeCollectionName);
    }
}
