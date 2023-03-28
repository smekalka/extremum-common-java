package io.extremum.rdf.triple.config;

import io.extremum.rdf.triple.dao.jpa.TripleJpaRepository;
import io.extremum.rdf.triple.service.JpaTripleService;
import io.extremum.rdf.triple.service.TripleService;
import io.extremum.rdf.triple.service.converter.TripleConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TripleJpaConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "extremum", value = "triple.storage.type", havingValue = "jpa")
    public TripleService tripleService(TripleJpaRepository dao, TripleConverter tripleConverter) {
        return new JpaTripleService(dao);
    }
}
