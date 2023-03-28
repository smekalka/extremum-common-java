package io.extremum.rdf.triple.service.converter;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.FromRequestDtoConverter;
import io.extremum.common.dto.converters.ReactiveToRequestDtoConverter;
import io.extremum.common.dto.converters.ReactiveToResponseDtoConverter;
import io.extremum.rdf.triple.controller.dto.TripleRequestDto;
import io.extremum.rdf.triple.controller.dto.TripleResponseDto;
import io.extremum.rdf.triple.dao.mongo.model.Triple;
import io.extremum.rdf.triple.model.ITriple;
import io.extremum.rdf.triple.service.IriResolver;
import io.extremum.sharedmodels.basic.StringOrObject;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class TripleConverter implements FromRequestDtoConverter<ITriple, TripleRequestDto>,
        ReactiveToRequestDtoConverter<ITriple, TripleRequestDto>,
        ReactiveToResponseDtoConverter<ITriple, TripleResponseDto> {

    @Lazy
    private final IriResolver iriResolver;

    @Override
    public Triple convertFromRequest(TripleRequestDto dto) {
        return new Triple(dto.getSubject(), dto.getPredicate(), dto.getObjects());
    }

    @Override
    public Mono<TripleRequestDto> convertToRequestReactively(ITriple model, ConversionConfig config) {
        return Mono.just(new TripleRequestDto(model.getSubject(), model.getPredicate(), model.getObjects()));
    }

    @Override
    public Class<? extends TripleRequestDto> getRequestDtoType() {
        return TripleRequestDto.class;
    }

    @Override
    public Mono<TripleResponseDto> convertToResponseReactively(ITriple model, ConversionConfig config) {
        if (config.isExpand()) {
            return Flux.fromIterable(model.getObjects())
                    .flatMap(s -> iriResolver.resolve(s).map(StringOrObject::new).defaultIfEmpty(new StringOrObject<>(s)))
                    .collectList()
                    .map(resolvedList -> new TripleResponseDto(
                                    model.getSubject(),
                                    model.getPredicate(),
                                    resolvedList
                            )
                    );
        } else {
            return Mono.just(
                    new TripleResponseDto(
                            model.getSubject(),
                            model.getPredicate(),
                            model.getObjects().stream().map(m -> new StringOrObject<ResponseDto>(m)).collect(Collectors.toList())
                    )
            );
        }
    }

    @Override
    public Class<? extends TripleResponseDto> getResponseDtoType() {
        return TripleResponseDto.class;
    }

    @Override
    public String getSupportedModel() {
        return "triple";
    }
}