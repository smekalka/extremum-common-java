package io.extremum.everything.services.management;

import io.extremum.common.collection.service.ReactiveCollectionDescriptorExtractor;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.Response;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ReactiveGetDemultiplexerOnDescriptor implements ReactiveGetDemultiplexer {
    private final ReactiveEverythingManagementService evrEvrManagementService;
    private final EverythingCollectionManagementService collectionManagementService;
    private final ReactiveDescriptorService reactiveDescriptorService;
    private final ReactiveCollectionDescriptorExtractor collectionExtractor;
    private final EverythingOwnedModelManagementService ownedModelService;

    @Override
    public Mono<Response> get(String id, Projection projection, boolean expand) {
        return reactiveDescriptorService.loadByExternalId(id)
                .flatMap(descriptor -> {
                    return collectionExtractor.typeForGetOperation(descriptor)
                            .flatMap(type -> fetchForType(type, descriptor, projection, expand));
                });
    }

    private Mono<? extends Response> fetchForType(Descriptor.Type type, Descriptor id,
                                                  Projection projection, boolean expand) {
        switch (type) {
            case COLLECTION:
                return fetchCollection(id, projection, expand);
            case SINGLE:
                return evrEvrManagementService.get(id, expand)
                        .map(Response::ok);
            case OWNED:
                return fetchOwned(id, expand);
            default:
                throw new EverythingEverythingException(
                        String.format("'%s' is neither single nor collection", id.getExternalId()));
        }
    }

    private Mono<Response> fetchCollection(Descriptor id, Projection projection, boolean expand) {
        return collectionManagementService.fetchCollectionReactively(id, projection, expand);
    }

    private Mono<Response> fetchOwned(Descriptor id, boolean expand) {
        return ownedModelService.fetchOwnedModelReactively(id, expand);
    }
}
