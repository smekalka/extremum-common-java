package io.extremum.dynamic.watch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.POJONode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.RemoveOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import io.extremum.common.exceptions.ProgrammingErrorException;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.processor.ReactiveWatchEventConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultDynamicModelWatchService implements DynamicModelWatchService {
    private final ReactiveWatchEventConsumer watchEventConsumer;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> registerPatchOperation(JsonPatch patch, JsonDynamicModel patched) {
        return processPatchInvocation(patch, patched);
    }

    @Override
    public Mono<Void> registerSaveOperation(JsonDynamicModel saved) {
        return processSaveInvocation(saved);
    }

    @Override
    public Mono<Void> registerDeleteOperation(JsonDynamicModel model) {
        return processDeleteInvocation(model);
    }

    private Mono<Void> processPatchInvocation(JsonPatch jsonPatch, JsonDynamicModel model) {
        return model.getId()
                .getInternalIdReactively()
                .flatMap(internalId -> {
                    try {
                        String jsonPatchString = objectMapper.writeValueAsString(jsonPatch);
                        log.debug("Convert JsonPatch into string {}", jsonPatchString);

                        String fullReplacePatchString = constructFullReplaceJsonPatch(model);

                        TextWatchEvent event = new TextWatchEvent(jsonPatchString, fullReplacePatchString, internalId, model);
                        return watchEventConsumer.consume(event);
                    } catch (Exception e) {
                        log.error("Unable to watch 'patch' for model {}", model, e);
                        return Mono.error(e);
                    }
                }).then();
    }

    private Mono<Void> processDeleteInvocation(JsonDynamicModel model) {
        return model.getId()
                .getInternalIdReactively()
                .flatMap(internalId -> {
                    try {
                        String jsonPatch = constructFullRemovalJsonPatch();
                        TextWatchEvent event = new TextWatchEvent(jsonPatch, null, internalId, model);
                        event.touchModelMotificationTime();
                        return watchEventConsumer.consume(event);
                    } catch (Exception e) {
                        log.error("Unable to watch 'delete' operation for model {}", model, e);
                        return Mono.error(e);
                    }
                }).then();
    }

    private Mono<Void> processSaveInvocation(JsonDynamicModel model) {
        return model.getId().getInternalIdReactively()
                .flatMap(internalId -> {
                    if (log.isDebugEnabled()) {
                        log.debug("Watch method with name 'save' and args saved model {}", model);
                    }

                    try {
                        String jsonPatchString = constructFullReplaceJsonPatch(model.getModelData());
                        TextWatchEvent event = new TextWatchEvent(jsonPatchString, null, internalId, model);
                        return watchEventConsumer.consume(event);
                    } catch (Exception e) {
                        log.error("Unable to watch a 'save' invocation for model {}", model, e);
                        return Mono.error(e);
                    }
                }).then();
    }

    private String constructFullRemovalJsonPatch() throws JsonProcessingException {
        RemoveOperation operation = new RemoveOperation(rootPointer());
        return serializeSingleOperationPatch(operation);
    }

    private String constructFullReplaceJsonPatch(Object model) throws JsonProcessingException {
        ReplaceOperation operation = new ReplaceOperation(rootPointer(), new POJONode(model));
        return serializeSingleOperationPatch(operation);
    }

    private JsonPointer rootPointer() {
        try {
            return new JsonPointer("/");
        } catch (JsonPointerException e) {
            throw new ProgrammingErrorException("Invalid JSON pointer", e);
        }
    }

    private String serializeSingleOperationPatch(JsonPatchOperation operation) throws JsonProcessingException {
        JsonPatch jsonPatch = new JsonPatch(Collections.singletonList(operation));
        return objectMapper.writeValueAsString(jsonPatch);
    }
}
