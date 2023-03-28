package io.extremum.common.descriptor.end2end.fixture;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.ReactiveToResponseDtoConverter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TestMongoModelConverter implements ReactiveToResponseDtoConverter<TestMongoModel, TestMongoModelResponseDto> {
    @Override
    public Mono<TestMongoModelResponseDto> convertToResponseReactively(TestMongoModel model, ConversionConfig config) {
        TestMongoModelResponseDto dto = new TestMongoModelResponseDto();
        dto.setId(model.getUuid());
        dto.setCreated(model.getCreated());
        dto.setModified(model.getModified());
        dto.setVersion(model.getVersion());
        dto.setNumber(model.getNumber());
        return Mono.just(dto);
    }

    @Override
    public Class<? extends TestMongoModelResponseDto> getResponseDtoType() {
        return TestMongoModelResponseDto.class;
    }

    @Override
    public String getSupportedModel() {
        return TestMongoModel.MODEL_NAME;
    }
}
