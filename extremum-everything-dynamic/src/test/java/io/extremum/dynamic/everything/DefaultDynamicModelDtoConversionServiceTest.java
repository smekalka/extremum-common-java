package io.extremum.dynamic.everything;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.dynamic.everything.dto.JsonDynamicModelResponseDto;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.extremum.sharedmodels.basic.Model.FIELDS.*;

class DefaultDynamicModelDtoConversionServiceTest {
    @Test
    void convertToResponseDtoReactively() throws IOException {
        DefaultDynamicModelDtoConversionService service = new DefaultDynamicModelDtoConversionService();

        Descriptor descriptor = Descriptor.builder()
                .internalId("internal-id")
                .externalId("external-id")
                .modelType("DynModel_A")
                .build();


        Map<String, Object> data = new HashMap<>();
        data.put("a", "b");
        data.put(created.name(), "2020-01-15T09:10:34.849000+0300");
        data.put(modified.name(), "2020-01-15T09:10:34.849000+0300");
        data.put(version.name(), 1);
        data.put(model.name(), "DynModel_A");

        JsonDynamicModel model = new JsonDynamicModel(descriptor, "DynModel_A", data);
        Mono<ResponseDto> result = service.convertToResponseDtoReactively(model, ConversionConfig.defaults());

        StepVerifier.create(result)
                .assertNext(dto -> {
                    Assertions.assertTrue(dto instanceof JsonDynamicModelResponseDto);

                    Assertions.assertEquals(model.getId(), dto.getId());
                    Assertions.assertEquals(model.getModelData(), ((JsonDynamicModelResponseDto) dto).getData());
                    Assertions.assertEquals(model.getModelName(), dto.getModel());
                })
                .verifyComplete();
    }
}
