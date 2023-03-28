package io.extremum.everything.services.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.exceptions.UnsupportedOperationException;
import io.extremum.common.model.CollectionFilter;
import io.extremum.common.modelservices.ModelServices;
import io.extremum.common.spring.data.OffsetBasedPageRequest;
import io.extremum.common.support.ModelClasses;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.services.RemovalService;
import io.extremum.everything.services.defaultservices.DefaultRemover;
import io.extremum.security.DataSecurity;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;

@RequiredArgsConstructor
public class DefaultEverythingEverythingManagementService implements EverythingEverythingManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEverythingEverythingManagementService.class);

    private final ModelRetriever modelRetriever;
    private final PatchFlow patchFlow;
    private final List<RemovalService> removalServices;
    private final DefaultRemover defaultRemover;
    private final DtoConversionService dtoConversionService;
    private final DataSecurity dataSecurity;
    private final ModelSaver modelSaver;
    private final ModelClasses modelClasses;
    private final ModelNames modelNames;
    private final EverythingCollectionManagementService collectionManagementService;

    @Override
    public ResponseDto get(Descriptor id, boolean expand) {
        Model model = modelRetriever.retrieveModel(id);

        dataSecurity.checkGetAllowed(model);

        if (model == null) {
            throw new ModelNotFoundException(String.format("Nothing was found by '%s'", id.getExternalId()));
        }

        ConversionConfig conversionConfig = ConversionConfig.builder().model(modelNames.determineModelName(id)).expand(expand).build();
        return convertModelToResponseDto(model, conversionConfig);
    }

    @Override
    public Response get(String iri, Projection projection, boolean expand) {
        String modelName = modelNames.determineModelNameByCollectionName(iri);
        if (modelName != null) {
            ConversionConfig conversionConfig = ConversionConfig.builder().model(modelName).expand(expand).build();
            Slice<Model> models = modelRetriever.retrieveModelPage(modelName, new OffsetBasedPageRequest(projection.getOffset().orElse(0), projection.getLimit().orElse(10)));
            if (!models.getContent().isEmpty()) {
                dataSecurity.checkGetAllowed(models.getContent().get(0));
            }

            return Response.ok(models.getContent().stream().map(model -> dtoConversionService.convertUnknownToResponseDto(model, conversionConfig)).collect(Collectors.toList()));
        }

        return null;
    }

    @Override
    public Response get(String iri, Projection projection, boolean expand, CollectionFilter collectionFilter) {
        String modelName = modelNames.determineModelNameByCollectionName(iri);
        if (modelName != null) {
            ConversionConfig conversionConfig = ConversionConfig.builder().model(modelName).expand(expand).build();
            Slice<Model> models = modelRetriever.retrieveModelPage(modelName, collectionFilter, new OffsetBasedPageRequest(projection.getOffset().orElse(0), projection.getLimit().orElse(10)));
            if (!models.getContent().isEmpty()) {
                dataSecurity.checkGetAllowed(models.getContent().get(0));
            }

            return Response.ok(models.getContent().stream().map(model -> dtoConversionService.convertUnknownToResponseDto(model, conversionConfig)).collect(Collectors.toList()));
        }

        return null;
    }

    private ResponseDto convertModelToResponseDto(Model model, ConversionConfig conversionConfig) {
        return dtoConversionService.convertUnknownToResponseDto(model, conversionConfig);
    }

    @Override
    public ResponseDto patch(Descriptor id, JsonPatch patch, boolean expand) {
        ConversionConfig conversionConfig = ConversionConfig.builder().model(modelNames.determineModelName(id)).expand(expand).build();

        Model patched = patchFlow.patch(id, patch);
        return convertModelToResponseDto(patched, conversionConfig);
    }

    @Override
    public void remove(Descriptor id) {
        checkDataSecurityAllowsRemoval(id);

        Remover remover = findRemover(id);
        remover.remove(id.getInternalId());
        LOGGER.debug(format("Model with ID '%s' was removed by service '%s'", id, remover));
    }

    private Remover findRemover(Descriptor id) {
        String modelName = modelNames.determineModelName(id);

        RemovalService removalService = ModelServices.findServiceForModel(modelName, removalServices);
        if (removalService != null) {
            return new NonDefaultRemover(removalService);
        }

        return defaultRemover;
    }

    @Override
    public void remove(Descriptor id, Descriptor nestedId) {
        if(id.effectiveType()!= Descriptor.Type.COLLECTION){
            throw new UnsupportedOperationException("Deleting of nested items is only supported for collections", 400);
        }

        collectionManagementService.removeFromCollection(id, nestedId);
    }

    private void checkDataSecurityAllowsRemoval(Descriptor id) {
        Model model = modelRetriever.retrieveModel(id);
        dataSecurity.checkRemovalAllowed(model);
    }

    @Override
    public ResponseDto create(String collectionName, RequestDto requestDto, boolean expand) {
        try {
            UUID uuid = UUID.fromString(collectionName);
            Descriptor descriptor = new Descriptor(uuid.toString());
            if (descriptor.effectiveType() == Descriptor.Type.COLLECTION) {
                String modelName = modelNames.determineModelNameByCollectionName(collectionName);
                Model model = ((DefaultEverythingCollectionManagementService) collectionManagementService).addToCollection(descriptor, requestDto);
                return dtoConversionService.convertUnknownToResponseDto(model, ConversionConfig.builder().model(modelName).expand(expand).build());
            } else {
                throw new IllegalArgumentException("Should be collection descriptor");
            }
        } catch (IllegalArgumentException exception) {
            String modelName = modelNames.determineModelNameByCollectionName(collectionName);
            Class<Model> classByModelName = modelClasses.getClassByModelName(modelName);
            Model model = dtoConversionService.convertFromRequestDto(classByModelName, requestDto);
            dataSecurity.checkCreateAllowed(model);

            return dtoConversionService
                    .convertUnknownToResponseDto(
                            modelSaver.saveModel(model),
                            ConversionConfig.builder().expand(expand).model(modelName).build()
                    );
        }
    }
}