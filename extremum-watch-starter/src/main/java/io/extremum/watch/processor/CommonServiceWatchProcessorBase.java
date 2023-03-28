package io.extremum.watch.processor;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.ModelClasses;
import io.extremum.watch.annotation.CapturedModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.watch.models.TextWatchEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import static io.extremum.watch.processor.JsonPatchUtils.constructFullRemovalJsonPatch;

/**
 * Processor for {@link CommonService} pointcut
 */
@RequiredArgsConstructor
@Slf4j
public abstract class CommonServiceWatchProcessorBase {
    protected final ObjectMapper objectMapper;
    protected final DescriptorService descriptorService;
    protected final ModelClasses modelClasses;
    protected final DtoConversionService dtoConversionService;

    protected void logInvocation(Invocation invocation) {
        if (log.isDebugEnabled()) {
            log.debug("Captured method {} with args {}", invocation.methodName(), Arrays.toString(invocation.args()));
        }
    }

    protected boolean isSaveMethod(Invocation invocation) {
        return "save".equals(invocation.methodName());
    }

    protected boolean isDeleteMethod(Invocation invocation) {
        return "delete".equals(invocation.methodName());
    }

    protected boolean isModelWatched(Model model) {
        Class<?> modelClass = model.getClass();
        return isModelClassWatched(modelClass);
    }

    protected boolean isModelClassWatched(Class<?> modelClass) {
        return modelClass.getAnnotation(CapturedModel.class) != null;
    }
}
