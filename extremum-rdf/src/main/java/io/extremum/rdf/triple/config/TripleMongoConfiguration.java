package io.extremum.rdf.triple.config;

import io.extremum.rdf.triple.dao.mongo.TripleMongoRepository;
import io.extremum.rdf.triple.service.MongoTripleService;
import io.extremum.rdf.triple.service.TripleService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class TripleMongoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "extremum", value = "triple.storage.type", havingValue = "mongo")
    public TripleService tripleService(TripleMongoRepository dao, ReactiveMongoTemplate mongoTemplate) {
        return new MongoTripleService(dao, mongoTemplate);
    }
}
