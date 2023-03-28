package io.extremum.common.dto.converters;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.basic.Model;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

@Service
public class StubDtoConverter implements ToResponseDtoConverter<Model, ResponseDto>,
        ReactiveToResponseDtoConverter<Model, ResponseDto> {
    @Override
    public ResponseDto convertToResponse(Model model, ConversionConfig config) {
        return new StubResponseDto();
    }

    @Override
    public Mono<ResponseDto> convertToResponseReactively(Model model, ConversionConfig config) {
        return Mono.just(new StubResponseDto());
    }

    @Override
    public Class<? extends ResponseDto> getResponseDtoType() {
        return ResponseDto.class;
    }

    @Override
    public String getSupportedModel() {
        return "StubModel";
    }

    private static class StubResponseDto implements ResponseDto {
        @Override
        public Descriptor getId() {
            return null;
        }

        @Override
        public Long getVersion() {
            return 0L;
        }

        @Override
        public ZonedDateTime getCreated() {
            return ZonedDateTime.now();
        }

        @Override
        public ZonedDateTime getModified() {
            return null;
        }

        @Override
        public String getModel() {
            return "stub";
        }
    }
}
