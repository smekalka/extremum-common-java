package io.extremum.rdf.triple.service;

import com.mongodb.client.result.UpdateResult;
import io.extremum.mongo.dao.ReactiveMongoCommonDao;
import io.extremum.rdf.triple.controller.dto.TripleDto;
import io.extremum.rdf.triple.dao.mongo.model.Triple;
import io.extremum.rdf.triple.model.ITriple;
import org.bson.BsonInt32;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoTripleServiceTest {

    @Mock
    private ReactiveMongoTemplate mongoTemplate;

    @Mock
    private ReactiveMongoCommonDao<Triple> dao;

    @Captor
    ArgumentCaptor<Update> updateArgumentCaptor;

    @Captor
    ArgumentCaptor<Query> queryArgumentCaptor;

    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;

    private MongoTripleService service;

    @BeforeEach
    void setUp() {
        service = new MongoTripleService(dao, mongoTemplate);
    }

    @Test
    void should_create_or_update_triple_properly() {
        when(mongoTemplate.upsert(any(), any(), any(String.class)))
                .thenReturn(Mono.just(UpdateResult.acknowledged(1, 1L, new BsonInt32(1))));

        TripleDto dto = new TripleDto();
        dto.setObj("testObj");
        dto.setPredicate("testPredicate");
        dto.setSubject("testSubject");

        StepVerifier
                .create(service.createOrUpdate(dto))
                .expectNext(true)
                .verifyComplete();
        verify(mongoTemplate).upsert(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), stringArgumentCaptor.capture());

        Query expectedQuery = new Query(Criteria.where("subject").is(dto.getSubject()).and("predicate").is(dto.getPredicate()));
        Assertions.assertEquals(expectedQuery, queryArgumentCaptor.getValue());

        Update expectedUpdate = new Update()
                .setOnInsert("subject", dto.getSubject())
                .setOnInsert("predicate", dto.getPredicate())
                .addToSet("objects", dto.getObj());
        Assertions.assertEquals(expectedUpdate, updateArgumentCaptor.getValue());

        Assertions.assertEquals("triple", stringArgumentCaptor.getValue());
    }

    @Test
    void should_delete_triple_properly() {
        when(mongoTemplate.updateMulti(any(), any(), any(String.class)))
                .thenReturn(Mono.just(UpdateResult.acknowledged(1, 1L, new BsonInt32(1))));

        TripleDto dto = new TripleDto();
        dto.setObj("testObj");
        dto.setPredicate("testPredicate");
        dto.setSubject("testSubject");

        StepVerifier
                .create(service.delete(dto))
                .expectNext(true)
                .verifyComplete();

        verify(mongoTemplate).updateMulti(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), stringArgumentCaptor.capture());

        Query expectedQuery = new Query(Criteria.where("subject").is(dto.getSubject()).and("predicate").is(dto.getPredicate()));
        Assertions.assertEquals(expectedQuery, queryArgumentCaptor.getValue());

        Update expectedUpdate = new Update()
                .setOnInsert("subject", dto.getSubject())
                .setOnInsert("predicate", dto.getPredicate())
                .pull("objects", dto.getObj());
        Assertions.assertEquals(expectedUpdate, updateArgumentCaptor.getValue());

        Assertions.assertEquals("triple", stringArgumentCaptor.getValue());
    }

    @Test
    void should_get_links_properly() {
        Query query = new Query(Criteria.where("subject").is("testSubject").and("predicate").is("testPredicate"));
        when(mongoTemplate.find(query, ITriple.class, "triple")).thenReturn(
                Flux.fromIterable(Arrays.asList(
                        new Triple("testSubject", "testPredicate", Arrays.asList("obj1", "obj2")),
                        new Triple("testSubject", "testPredicate", Arrays.asList("obj3", "obj4")),
                        new Triple("testSubject", "testPredicate", Arrays.asList("obj5", "obj6")))
                ));

        StepVerifier
                .create(service.getLinks("testSubject", "testPredicate", 2, 1))
                .expectNextMatches(next -> next.getSubject().equals("testSubject") &&
                        next.getPredicate().equals("testPredicate") &&
                        next.getObjects().equals(Arrays.asList("obj3", "obj4"))
                )
                .expectNextCount(1)
                .verifyComplete();

        query = new Query(Criteria.where("subject").is("testSubject"));
        when(mongoTemplate.find(query, ITriple.class, "triple")).thenReturn(
                Flux.fromIterable(Arrays.asList(
                        new Triple("testSubject", "testPredicate", new ArrayList<>(Arrays.asList("obj1", "obj2"))),
                        new Triple("testSubject", "testPredicate", new ArrayList<>(Arrays.asList("obj3", "obj4"))),
                        new Triple("testSubject", "testPredicate", new ArrayList<>(Arrays.asList("obj5", "obj6"))))
                ));
        StepVerifier
                .create(service.getLinks("testSubject", null, 1, 0))
                .expectNextMatches(next -> next.getSubject().equals("testSubject") &&
                        next.getPredicate().equals("testPredicate") &&
                        next.getObjects().equals(Arrays.asList("obj1", "obj2", "obj3", "obj4", "obj5", "obj6"))
                )
                .verifyComplete();
    }
}