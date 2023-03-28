package io.extremum.everything.services.collection;

import io.extremum.common.collection.conversion.CollectionMakeupModule;
import io.extremum.common.collection.conversion.CollectionMakeupRequest;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@RequiredArgsConstructor
public class FillCollectionTop implements CollectionMakeupModule {
    private final EverythingCollectionService everythingCollectionService;

    @Override
    public void applyToCollection(CollectionMakeupRequest request) {
        if (request.getReference().getTop() != null) {
            return;
        }

        CollectionFragment<ResponseDto> dtosFragment = everythingCollectionService.fetchCollection(
                request.getCollectionDescriptor().getCollection(),
                buildProjection(request.getCollectionDescriptor()), true);

        fillCollectionReferenceFromFragment(dtosFragment, request);
    }

    private Projection buildProjection(Descriptor collectionDescriptor) {
        return Projection.offsetLimit(0, determineLimit(collectionDescriptor));
    }

    private int determineLimit(Descriptor collectionDescriptor) {
        // TODO: apply dynamic limit calculation
        return 10;
    }

    private void fillCollectionReferenceFromFragment(CollectionFragment<ResponseDto> dtosFragment,
                                                     CollectionMakeupRequest request) {
        //noinspection unchecked
        request.getReference().setTop(new ArrayList(dtosFragment.elements()));
        dtosFragment.total().ifPresent(count -> {
            if (request.getReference().getCount() == null) {
                request.getReference().setCount(count);
            }
        });
    }

    @Override
    public Mono<Void> applyToCollectionReactively(CollectionMakeupRequest request) {
        if (request.getReference().getTop() != null) {
            return Mono.empty();
        }

        Mono<CollectionFragment<ResponseDto>> fragmentMono = everythingCollectionService.fetchCollectionReactively(
                request.getCollectionDescriptor().getCollection(),
                buildProjection(request.getCollectionDescriptor()), true);

        return fragmentMono
                .doOnNext(dtosFragment -> fillCollectionReferenceFromFragment(dtosFragment, request))
                .then();
    }
}
