package io.extremum.everything.services.collection;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.reactive.Reactifier;
import io.extremum.common.tx.CollectionTransactivity;
import io.extremum.common.utils.ModelUtils;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.everything.services.management.ModelSaver;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.ResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DefaultEverythingCollectionService implements EverythingCollectionService {
    private final DtoConversionService dtoConversionService;

    private final OwnedCoordinatesHandler ownedCoordinatesHandler;
    private final FreeCoordinatesHandler freeCoordinatesHandler;

    public DefaultEverythingCollectionService(ModelRetriever modelRetriever,
                                              CollectionProviders collectionProviders,
                                              DtoConversionService dtoConversionService, UniversalDao universalDao,
                                              Reactifier reactifier, CollectionTransactivity transactivity, ModelSaver modelSaver) {
        this.dtoConversionService = dtoConversionService;

        ownedCoordinatesHandler = new OwnedCoordinatesHandler(modelRetriever, universalDao,
                collectionProviders, reactifier, transactivity, modelSaver);
        freeCoordinatesHandler = new FreeCoordinatesHandler(collectionProviders);
    }

    @Override
    public CollectionFragment<ResponseDto> fetchCollection(CollectionDescriptor id,
                                                           Projection projection, boolean expand) {
        CoordinatesHandler coordinatesHandler = findCoordinatesHandler(id.getType());
        CollectionFragment<Model> fragment = coordinatesHandler.fetchCollection(id.getCoordinates(), projection);
        return fragment.map(model -> convertModelToResponseDto(model, expand));
    }

    @Override
    public Mono<CollectionFragment<ResponseDto>> fetchCollectionReactively(CollectionDescriptor id,
                                                                           Projection projection, boolean expand) {
        CoordinatesHandler coordinatesHandler = findCoordinatesHandler(id.getType());
        return coordinatesHandler.fetchCollectionReactively(id.getCoordinates(), projection)
                .flatMap(fragment -> convertFragmentToDtos(fragment, expand));
    }

    private Mono<CollectionFragment<ResponseDto>> convertFragmentToDtos(
            CollectionFragment<Model> fragment, boolean expand) {
        return fragment.mapReactively(model -> convertModelToResponseDtoReactively(model, expand));
    }

    @Override
    public Flux<ResponseDto> streamCollection(CollectionDescriptor id, Projection projection, boolean expand) {
        CoordinatesHandler coordinatesHandler = findCoordinatesHandler(id.getType());
        Flux<Model> models = coordinatesHandler.streamCollection(id.getCoordinates(), projection);
        return models.concatMap(model -> convertModelToResponseDtoReactively(model, expand));
    }

    private CoordinatesHandler findCoordinatesHandler(CollectionDescriptor.Type type) {
        if (type == CollectionDescriptor.Type.OWNED) {
            return ownedCoordinatesHandler;
        }
        if (type == CollectionDescriptor.Type.FREE) {
            return freeCoordinatesHandler;
        }

        throw new IllegalStateException("Unsupported type: " + type);
    }

    private ResponseDto convertModelToResponseDto(Model model, boolean expand) {
        ConversionConfig conversionConfig = ConversionConfig.builder().expand(expand).model(ModelUtils.getModelName(model)).build();
        return dtoConversionService.convertUnknownToResponseDto(model, conversionConfig);
    }

    private Mono<ResponseDto> convertModelToResponseDtoReactively(Model model, boolean expand) {
        ConversionConfig conversionConfig = ConversionConfig.builder().expand(expand).build();
        return dtoConversionService.convertUnknownToResponseDtoReactively(model, conversionConfig);
    }

    public Model addToCollection(Descriptor collectionDescriptor, RequestDto requestDto) {
        return ownedCoordinatesHandler.addToCollection(collectionDescriptor.getCollection().getCoordinates(), (dto, clazz) -> dtoConversionService.convertFromRequestDto(clazz, dto), requestDto);
    }

    public void removeFromCollection(Descriptor collectionDescriptor, Descriptor nestedId) {
        ownedCoordinatesHandler.removeFromCollection(collectionDescriptor.getCollection().getCoordinates(), nestedId);
    }
}
