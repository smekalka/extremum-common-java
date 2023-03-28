package io.extremum.watch.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.POJONode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.RemoveOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.exceptions.ProgrammingErrorException;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.RequestDto;
import reactor.core.publisher.Mono;

import java.util.Collections;

public class JsonPatchUtils {
    public static String constructFullReplaceJsonPatch(ObjectMapper objectMapper,
                                                       DtoConversionService dtoConversionService,
                                                       Model model) {
        RequestDto dto = dtoConversionService.convertUnknownToRequestDto(model, ConversionConfig.defaults());
        ReplaceOperation operation = new ReplaceOperation(rootPointer(), new POJONode(dto));
        return serializeSingleOperationPatch(objectMapper, operation);
    }

    public static Mono<String> constructFullReplaceJsonPatchReactively(ObjectMapper objectMapper,
                                                                       DtoConversionService dtoConversionService,
                                                                       Model model) {
        return dtoConversionService.convertUnknownToRequestDtoReactively(model, ConversionConfig.defaults()).map(dto -> {
            ReplaceOperation operation = new ReplaceOperation(rootPointer(), new POJONode(dto));
            return serializeSingleOperationPatch(objectMapper, operation);
        });
    }

    public static String constructFullRemovalJsonPatch(ObjectMapper objectMapper) {
        RemoveOperation operation = new RemoveOperation(rootPointer());
        return serializeSingleOperationPatch(objectMapper, operation);
    }

    private static String serializeSingleOperationPatch(ObjectMapper objectMapper,
                                                        JsonPatchOperation operation) {
        try {
            JsonPatch jsonPatch = new JsonPatch(Collections.singletonList(operation));
            return objectMapper.writeValueAsString(jsonPatch);
        } catch (JsonProcessingException e) {
            throw new ProgrammingErrorException("JSON serialization failed", e);
        }
    }

    private static JsonPointer rootPointer() {
        try {
            return new JsonPointer("/");
        } catch (JsonPointerException e) {
            throw new ProgrammingErrorException("Invalid JSON pointer", e);
        }
    }
}
