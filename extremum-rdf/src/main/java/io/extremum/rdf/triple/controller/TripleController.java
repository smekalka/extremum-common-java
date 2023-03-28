package io.extremum.rdf.triple.controller;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.everything.controllers.EverythingExceptionHandlerTarget;
import io.extremum.rdf.triple.controller.dto.TripleDto;
import io.extremum.rdf.triple.service.TripleService;
import io.extremum.rdf.triple.service.converter.TripleConverter;
import io.extremum.sharedmodels.dto.Response;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@EverythingExceptionHandlerTarget
public class TripleController {

    private final TripleService service;

    private final TripleConverter converter;

    @GetMapping("/links")
    public Mono<Response> getLinks(
            @RequestParam String subject,
            @RequestParam(required = false) String predicate,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "false") boolean expand
    ) {
        return service
                .getLinks(subject, predicate, limit, offset)
                .flatMap(
                        triple -> converter.convertToResponseReactively(triple, ConversionConfig.builder().expand(expand).build())
                ).collectList()
                .map(Response::ok);
    }

    @PostMapping("/links")
    public Mono<Response> updateLinks(@RequestBody TripleDto tripleDto) {
        if (tripleDto.getDelete()) {
            return service.delete(tripleDto).map(Response::ok);
        }

        return service.createOrUpdate(tripleDto).map(
                Response::ok
        );
    }
}