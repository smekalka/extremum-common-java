package io.extremum.everything.services.management;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.everything.destroyer.EmptyFieldDestroyer;
import io.extremum.everything.services.RequestDtoValidator;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.RequestDto;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
public final class ReactivePatcherImpl implements ReactivePatcher {
    private final DtoConversionService dtoConversionService;
    private final ObjectMapper jsonMapper;
    private final PatcherHooksCollection hooksCollection;

    private final PatchingSupport patchingSupport;

    public ReactivePatcherImpl(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
                               EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator,
                               PatcherHooksCollection hooksCollection) {
        Objects.requireNonNull(dtoConversionService, "dtoConversionService cannot be null");
        Objects.requireNonNull(jsonMapper, "jsonMapper cannot be null");
        Objects.requireNonNull(emptyFieldDestroyer, "emptyFieldDestroyer cannot be null");
        Objects.requireNonNull(dtoValidator, "dtoValidator cannot be null");
        Objects.requireNonNull(hooksCollection, "hooksCollection cannot be null");

        this.dtoConversionService = dtoConversionService;
        this.jsonMapper = jsonMapper;
        this.hooksCollection = hooksCollection;

        patchingSupport = new PatchingSupport(jsonMapper, dtoValidator, dtoConversionService, emptyFieldDestroyer);
    }

    @Override
    public final Mono<Model> patch(Descriptor id, Model modelToPatch, JsonPatch patch) {
        return applyPatch(patch, modelToPatch)
                .doOnNext(patchedDto -> hooksCollection.afterPatchAppliedToDto(id.getModelType(), modelToPatch, patchedDto))
                .doOnNext(patchingSupport::validateRequest)
                .map(patchedDto -> patchingSupport.assemblePatchedModel(patchedDto, modelToPatch));
    }

    private Mono<RequestDto> applyPatch(JsonPatch patch, Model modelToPatch) {
        return modelToJsonNode(modelToPatch)
                .map(nodeToPatch -> patchingSupport.applyPatchToNode(patch, nodeToPatch))
                .map(patchedNode -> {
                    Class<? extends RequestDto> requestDtoType = dtoConversionService.findReactiveRequestDtoType(
                            modelToPatch.getClass());
                    return patchingSupport.nodeToRequestDto(patchedNode, requestDtoType);
                });
    }

    private Mono<JsonNode> modelToJsonNode(Model model) {
        return dtoConversionService.convertUnknownToRequestDtoReactively(model, ConversionConfig.defaults())
                .map(jsonMapper::valueToTree);
    }
}
