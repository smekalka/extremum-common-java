package io.extremum.everything.services.management;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.everything.destroyer.EmptyFieldDestroyer;
import io.extremum.everything.exceptions.RequestDtoValidationException;
import io.extremum.everything.services.RequestDtoValidator;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.RequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
class PatchingSupport {
    private final ObjectMapper jsonMapper;
    private final RequestDtoValidator dtoValidator;
    private final DtoConversionService dtoConversionService;
    private final EmptyFieldDestroyer emptyFieldDestroyer;

    JsonNode applyPatchToNode(JsonPatch patch, JsonNode target) {
        try {
            return patch.apply(target);
        } catch (JsonPatchException e) {
            String message = format("Unable to apply patch %s to json %s", patch, target);
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    RequestDto nodeToRequestDto(JsonNode patchedNode, Class<? extends RequestDto> requestDtoType) {
        try {
            return jsonMapper.treeToValue(patchedNode, requestDtoType);
        } catch (JsonProcessingException e) {
            String message = format("Unable to create a type %s from a raw json data %s", requestDtoType, patchedNode);
            log.error(message);
            throw new RuntimeException(message);
        }
    }

    void validateRequest(RequestDto dto) {
        Set<ConstraintViolation<RequestDto>> constraintViolation = dtoValidator.validate(dto);
        processValidationResult(dto, constraintViolation);
    }

    Model assemblePatchedModel(RequestDto patchedDto, Model modelToPatch) {
        Model patchedModel = dtoConversionService.convertFromRequestDto(modelToPatch.getClass(), patchedDto);
        modelToPatch.copyServiceFieldsTo(patchedModel);
        return emptyFieldDestroyer.destroy(patchedModel);
    }

    private void processValidationResult(RequestDto dto,
                                         Set<ConstraintViolation<RequestDto>> constraintsViolation) {
        if (!constraintsViolation.isEmpty()) {
            log.error("Invalid requestDto DTO after patching detected {}", constraintsViolation);
            throw new RequestDtoValidationException(dto, constraintsViolation);
        }
    }
}
