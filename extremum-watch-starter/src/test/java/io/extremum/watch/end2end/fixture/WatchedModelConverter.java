package io.extremum.watch.end2end.fixture;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.FromRequestDtoConverter;
import io.extremum.common.dto.converters.ReactiveToRequestDtoConverter;
import io.extremum.common.dto.converters.ToRequestDtoConverter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
@Service
public class WatchedModelConverter implements ToRequestDtoConverter<WatchedModel, WatchedModelRequestDto>,
        ReactiveToRequestDtoConverter<WatchedModel, WatchedModelRequestDto>,
        FromRequestDtoConverter<WatchedModel, WatchedModelRequestDto> {
    @Override
    public WatchedModelRequestDto convertToRequest(WatchedModel model, ConversionConfig config) {
        WatchedModelRequestDto dto = new WatchedModelRequestDto();
        dto.setName(model.getName());
        return dto;
    }

    @Override
    public Mono<WatchedModelRequestDto> convertToRequestReactively(WatchedModel model, ConversionConfig config) {
        WatchedModelRequestDto dto = new WatchedModelRequestDto();
        dto.setName(model.getName());
        return Mono.just(dto);
    }

    @Override
    public WatchedModel convertFromRequest(WatchedModelRequestDto dto) {
        WatchedModel model = new WatchedModel();
        model.setName(dto.getName());
        return model;
    }

    @Override
    public Class<? extends WatchedModelRequestDto> getRequestDtoType() {
        return WatchedModelRequestDto.class;
    }

    @Override
    public String getSupportedModel() {
        return WatchedModel.MODEL_NAME;
    }
}
