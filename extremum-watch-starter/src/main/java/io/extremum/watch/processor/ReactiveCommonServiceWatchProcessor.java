package io.extremum.watch.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.ModelClasses;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.watch.config.conditional.ReactiveWatchConfiguration;
import io.extremum.watch.models.TextWatchEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static io.extremum.watch.processor.JsonPatchUtils.constructFullRemovalJsonPatch;
import static io.extremum.watch.processor.JsonPatchUtils.constructFullReplaceJsonPatchReactively;

/**
 * Processor for {@link CommonService} pointcut
 */
@Slf4j
@Service
@ConditionalOnBean(ReactiveWatchConfiguration.class)
public class ReactiveCommonServiceWatchProcessor extends CommonServiceWatchProcessorBase {
    private final ReactiveWatchEventConsumer watchEventConsumer;

    public ReactiveCommonServiceWatchProcessor(ObjectMapper objectMapper,
                                               DescriptorService descriptorService,
                                               ModelClasses modelClasses,
                                               DtoConversionService dtoConversionService,
                                               ReactiveWatchEventConsumer watchEventConsumer) {
        super(objectMapper, descriptorService, modelClasses, dtoConversionService);
        this.watchEventConsumer = watchEventConsumer;
    }

    public Mono<Void> process(Invocation invocation, Model returnedModel) throws JsonProcessingException {
        logInvocation(invocation);

        if (isSaveMethod(invocation)) {
            return processSave(invocation.args());
        } else if (isDeleteMethod(invocation)) {
            return processDeletion(returnedModel, invocation.args());
        } else {
            return Mono.empty();
        }
    }

    private Mono<Void> processSave(Object[] args) {
        Model model = (Model) args[0];
        if (isModelWatched(model) && model instanceof BasicModel) {
            return constructFullReplaceJsonPatchReactively(objectMapper, dtoConversionService, model)
                    .flatMap(jsonPatchString -> {
                        String modelInternalId = ((BasicModel<?>) model).getId().toString();
                        TextWatchEvent event = new TextWatchEvent(jsonPatchString, null, modelInternalId, model);
                        return watchEventConsumer.consume(event);
                    });
        } else {
            return Mono.empty();
        }
    }

    private Mono<Void> processDeletion(Model returnedModel, Object[] args) {
        String modelInternalId = (String) args[0];
        Descriptor descriptor = descriptorService.loadByInternalId(modelInternalId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Did not find a descriptor by internal ID '%s'", modelInternalId)));
        Class<Model> modelClass = modelClasses.getClassByModelName(descriptor.getModelType());

        if (isModelClassWatched(modelClass)) {
            String jsonPatch = constructFullRemovalJsonPatch(objectMapper);
            TextWatchEvent event = new TextWatchEvent(jsonPatch, null, modelInternalId, returnedModel);
            // TODO: should we just ALWAYS set modification time in CommonService.delete()?
            event.touchModelMotificationTime();
            return watchEventConsumer.consume(event);
        } else {
            return Mono.empty();
        }
    }
}
