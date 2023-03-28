package io.extremum.rdf.triple.service;

import io.extremum.rdf.triple.controller.dto.TripleDto;
import io.extremum.rdf.triple.model.ITriple;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TripleService {

    Mono<Boolean> createOrUpdate(TripleDto requestDto);

    Mono<Boolean> delete(TripleDto tripleDto);

    Flux<ITriple> getLinks(String subject, String predicate, int limit, int offset);
}
