package io.extremum.everything.services.management;

import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.services.collection.DefaultEverythingCollectionService;
import io.extremum.everything.services.collection.EverythingCollectionService;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.Pagination;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static io.extremum.sharedmodels.dto.Response.ok;

@RequiredArgsConstructor
public class DefaultEverythingCollectionManagementService implements EverythingCollectionManagementService {
    private final ReactiveCollectionDescriptorService reactiveCollectionDescriptorService;
    private final EverythingCollectionService everythingCollectionService;

    @Override
    public Response fetchCollection(Descriptor descriptor, Projection projection, boolean expand) {
        CollectionDescriptor collectionDescriptor = getRequiredCollectionDescriptor(descriptor);

        CollectionFragment<ResponseDto> fragment = everythingCollectionService.fetchCollection(
                collectionDescriptor, projection, expand);

        return createResponse(fragment, projection);
    }

    private CollectionDescriptor getRequiredCollectionDescriptor(Descriptor descriptor) {
        CollectionDescriptor collectionDescriptor = descriptor.getCollection();
        if (collectionDescriptor == null) {
            throw new IllegalStateException(
                    String.format("For '%s' no collection was in the descriptor", descriptor.getExternalId()));
        }
        return collectionDescriptor;
    }

    private Response createResponse(CollectionFragment<ResponseDto> fragment, Projection projection) {
        Pagination.PaginationBuilder paginationBuilder = Pagination.builder()
                .count(fragment.elements().size());
        projection.getOffset().ifPresent(paginationBuilder::offset);
        fragment.total().ifPresent(paginationBuilder::total);
        projection.getSince().ifPresent(paginationBuilder::since);
        projection.getUntil().ifPresent(paginationBuilder::until);
        Pagination pagination = paginationBuilder.build();

        return ok(fragment.elements(), pagination);
    }

    @Override
    public Mono<Response> fetchCollectionReactively(Descriptor collectionId, Projection projection, boolean expand) {
        return reactiveCollectionDescriptorService.retrieveByExternalId(collectionId.getExternalId())
                .flatMap(collectionDescriptor -> everythingCollectionService.fetchCollectionReactively(
                        collectionDescriptor, projection, expand))
                .map(collectionFragment -> createResponse(collectionFragment, projection));
    }

    @Override
    public Flux<ResponseDto> streamCollection(String collectionId, Projection projection, boolean expand) {
        return reactiveCollectionDescriptorService.retrieveByExternalId(collectionId)
                .flatMapMany(collDescriptor
                        -> everythingCollectionService.streamCollection(collDescriptor, projection, expand));
    }

    public Model addToCollection(Descriptor descriptor, RequestDto requestDto) {
       return ((DefaultEverythingCollectionService) everythingCollectionService).addToCollection(descriptor, requestDto);
    }

    @Override
    public void removeFromCollection(Descriptor collectionId, Descriptor nestedId) {
         ((DefaultEverythingCollectionService) everythingCollectionService).removeFromCollection(collectionId, nestedId);
    }
}
