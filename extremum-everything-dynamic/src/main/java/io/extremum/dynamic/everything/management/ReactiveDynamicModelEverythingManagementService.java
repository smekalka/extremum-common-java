package io.extremum.dynamic.everything.management;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DynamicModelDtoConversionService;
import io.extremum.common.exceptions.CommonException;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.datetime.DateUtils;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.JsonBasedDynamicModelService;
import io.extremum.dynamic.watch.DynamicModelWatchService;
import io.extremum.everything.services.management.ReactiveEverythingManagementService;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.constant.HttpStatus;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Map;

import static io.extremum.sharedmodels.basic.Model.FIELDS.created;
import static io.extremum.sharedmodels.basic.Model.FIELDS.modified;
import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public class ReactiveDynamicModelEverythingManagementService implements ReactiveEverythingManagementService {
    private final JsonBasedDynamicModelService dynamicModelService;
    private final DynamicModelDtoConversionService dynamicModelDtoConversionService;
    private final ObjectMapper mapper;
    private final DynamicModelWatchService watchService;

    @Override
    public Mono<ResponseDto> get(Descriptor id, boolean expand) {
        return dynamicModelService.findById(id)
                .onErrorMap(DescriptorNotFoundException.class,
                        cause -> newModelNotFoundException(id, cause))
                .flatMap(this::convertDynamicModelToResponseDto);
    }

    private ModelNotFoundException newModelNotFoundException(Descriptor id, Throwable cause) {
        return new ModelNotFoundException(format("Nothing was found by '%s'", id.getExternalId()), cause);
    }

    private Mono<ResponseDto> convertDynamicModelToResponseDto(Model model) {
        return dynamicModelDtoConversionService.convertToResponseDtoReactively(model, ConversionConfig.defaults());
    }

    @Override
    public Mono<ResponseDto> patch(Descriptor id, JsonPatch patch, boolean expand) {
        return dynamicModelService.findById(id)
                .map(JsonDynamicModel::getModelData)
                .map(data -> applyPatch(patch, data))
                .map(applied -> new JsonDynamicModel(id, id.getModelType(), applied))
                .flatMap(dynamicModelService::saveModelWithoutNotifications)
                .flatMap(saved -> watchService.registerPatchOperation(patch, saved).thenReturn(saved))
                .flatMap(this::convertDynamicModelToResponseDto)
                .onErrorMap(DescriptorNotFoundException.class, cause -> {
                    String msg = format("Model with id %s not found; nothing to patch with %s", id, patch);
                    log.warn(msg, cause);
                    return new ModelNotFoundException(msg, cause);
                });
    }

    private Map<String, Object> applyPatch(JsonPatch patch, Map<String, Object> map) {
        JsonNode node = mapper.convertValue(map, JsonNode.class);
        try {
            JsonNode patched = patch.apply(node);
            Map<String, Object> raw = mapper.convertValue(patched, new TypeReference<Map<String, Object>>() {
            });

            Object createdValue = raw.get(created.name());
            if (createdValue instanceof String) {
                raw.replace(created.name(), DateUtils.convert((String) createdValue));
            }

            Object modifiedValue = raw.get(modified.name());
            if (modifiedValue instanceof String) {
                raw.replace(modified.name(), DateUtils.convert((String) modifiedValue));
            }

            return raw;
        } catch (JsonPatchException e) {
            String msg = format("Unable to apply patch %s with node %s", patch, node);
            log.error(msg, e);
            throw new CommonException(msg, HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
        }
    }

    @Override
    public Mono<Void> remove(Descriptor id) {
        return dynamicModelService.remove(id)
                .onErrorResume(DescriptorNotFoundException.class, _it -> Mono.empty())
                .then();
    }
}
