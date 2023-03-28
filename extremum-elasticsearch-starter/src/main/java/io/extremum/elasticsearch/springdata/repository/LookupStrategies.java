package io.extremum.elasticsearch.springdata.repository;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.repository.query.QueryLookupStrategy;

import java.util.Optional;

/**
 * @author rpuch
 */
class LookupStrategies {
    private final ElasticsearchOperations elasticsearchOperations;

    LookupStrategies(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<QueryLookupStrategy> softDeleteQueryLookupStrategy(Optional<QueryLookupStrategy> optStrategy) {
        return optStrategy.map(this::createSoftDeleteQueryLookupStrategy);
    }

    private SoftDeleteElasticsearchQueryLookupStrategy createSoftDeleteQueryLookupStrategy(QueryLookupStrategy strategy) {
        return new SoftDeleteElasticsearchQueryLookupStrategy(strategy, elasticsearchOperations);
    }
}