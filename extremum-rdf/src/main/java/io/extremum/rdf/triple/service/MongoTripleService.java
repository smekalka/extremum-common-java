package io.extremum.rdf.triple.service;

import io.extremum.common.iri.properties.IriProperties;
import io.extremum.common.iri.service.DefaultIriFacilities;
import io.extremum.mongo.dao.ReactiveMongoCommonDao;
import io.extremum.mongo.service.impl.ReactiveMongoCommonServiceImpl;
import io.extremum.rdf.triple.controller.dto.TripleDto;
import io.extremum.rdf.triple.dao.mongo.model.Triple;
import io.extremum.rdf.triple.model.ITriple;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

public class MongoTripleService extends ReactiveMongoCommonServiceImpl<Triple> implements TripleService {

    public MongoTripleService(
            ReactiveMongoCommonDao<Triple> dao,
            ReactiveMongoTemplate mongoTemplate
    ) {
        super(dao, new DefaultIriFacilities(new IriProperties()));
        this.mongoTemplate = mongoTemplate;
    }

    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<Boolean> createOrUpdate(TripleDto tripleDto) {
        Query query = new Query(Criteria.where("subject").is(tripleDto.getSubject()).and("predicate").is(tripleDto.getPredicate()));
        Update update = new Update()
                .setOnInsert("subject", tripleDto.getSubject())
                .setOnInsert("predicate", tripleDto.getPredicate())
                .addToSet("objects", tripleDto.getObj());

        return mongoTemplate.upsert(query, update, "triple").map(updateResult -> updateResult.getModifiedCount() != 0 || updateResult.getUpsertedId() != null);
    }

    @Override
    public Mono<Boolean> delete(TripleDto tripleDto) {
        Query query = new Query(Criteria.where("subject").is(tripleDto.getSubject()).and("predicate").is(tripleDto.getPredicate()));
        Update update = new Update()
                .setOnInsert("subject", tripleDto.getSubject())
                .setOnInsert("predicate", tripleDto.getPredicate())
                .pull("objects", tripleDto.getObj());

        return mongoTemplate.updateMulti(query, update, "triple").map(updateResult -> updateResult.getModifiedCount() != 0 || updateResult.getUpsertedId() != null);
    }

    @Override
    public Flux<ITriple> getLinks(String subject, String predicate, int limit, int offset) {
        if (predicate != null) {
            return getForSubjectAndPredicate(subject, predicate)
                    .skip(offset)
                    .take(limit);
        } else {
            return getForSubject(subject)
                    .skip(offset)
                    .take(limit);
        }
    }

    private Flux<ITriple> getForSubjectAndPredicate(String subject, String predicate) {
        Query query = new Query(Criteria.where("subject").is(subject).and("predicate").is(predicate));
        return mongoTemplate.find(query, ITriple.class, "triple");

    }

    private Flux<ITriple> getForSubject(String subject) {
        Query query = new Query(Criteria.where("subject").is(subject));
        Flux<GroupedFlux<String, ITriple>> grouped = mongoTemplate.find(query, ITriple.class, "triple").groupBy(
                ITriple::getPredicate
        );

        return grouped.flatMap(
                group -> group.reduce((acc, item) -> {
                    acc.getObjects().addAll(item.getObjects());
                    return new Triple(acc.getSubject(), acc.getPredicate(), acc.getObjects());
                }));
    }
}