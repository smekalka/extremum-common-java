package io.extremum.rdf.triple.service;

import io.extremum.common.spring.data.OffsetBasedPageRequest;
import io.extremum.jpa.service.impl.PostgresCommonServiceImpl;
import io.extremum.rdf.triple.controller.dto.TripleDto;
import io.extremum.rdf.triple.dao.jpa.TripleJpaRepository;
import io.extremum.rdf.triple.dao.jpa.model.Triple;
import io.extremum.rdf.triple.model.ITriple;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;

public class JpaTripleService extends PostgresCommonServiceImpl<Triple> implements TripleService {

    private final TripleJpaRepository dao;

    public JpaTripleService(TripleJpaRepository dao) {
        super(dao);
        this.dao = dao;
    }

    @Override
    public Mono<Boolean> createOrUpdate(TripleDto requestDto) {
        Triple bySubjectAndPredicate = dao.findBySubjectAndPredicate(requestDto.getSubject(), requestDto.getPredicate());
        if (bySubjectAndPredicate != null) {
            bySubjectAndPredicate.getObjects().add(requestDto.getObj());
            this.save(bySubjectAndPredicate);
        } else {
            this.save(new Triple(requestDto.getSubject(), requestDto.getPredicate(), new HashSet<>(Collections.singletonList(requestDto.getObj()))));
        }

        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> delete(TripleDto tripleDto) {
        Triple bySubjectAndPredicate = dao.findBySubjectAndPredicate(tripleDto.getSubject(), tripleDto.getPredicate());
        if (bySubjectAndPredicate != null) {
            bySubjectAndPredicate.getObjects().remove(tripleDto.getObj());
            this.save(bySubjectAndPredicate);

            return Mono.just(true);
        } else {
            return Mono.just(false);
        }
    }

    @Override
    public Flux<ITriple> getLinks(String subject, String predicate, int limit, int offset) {
        if (predicate != null) {
            return Flux.fromIterable(dao.findAllBySubjectAndPredicate(subject, predicate, new OffsetBasedPageRequest(offset, limit)));
        } else {

            Flux<GroupedFlux<String, Triple>> grouped = Flux.fromIterable(dao.findAllBySubject(subject, new OffsetBasedPageRequest(offset, limit))).groupBy(
                    ITriple::getPredicate
            );

            return grouped.flatMap(
                    group -> group.reduce((acc, item) -> {
                        acc.getObjects().addAll(item.getObjects());
                        return new Triple(acc.getSubject(), acc.getPredicate(), acc.getObjects());
                    }));
        }
    }
}
