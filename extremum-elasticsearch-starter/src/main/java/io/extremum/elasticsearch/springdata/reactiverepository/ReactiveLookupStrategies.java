package io.extremum.elasticsearch.springdata.reactiverepository;

import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.repository.query.QueryLookupStrategy;

import java.util.Optional;

/**
 * @author rpuch
 */
class ReactiveLookupStrategies {
    private final ReactiveElasticsearchOperations elasticsearchOperations;

    ReactiveLookupStrategies(ReactiveElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<QueryLookupStrategy> softDeleteQueryLookupStrategy(Optional<QueryLookupStrategy> optStrategy) {
        return optStrategy.map(this::createSoftDeleteQueryLookupStrategy);
    }

    private SoftDeleteReactiveElasticsearchQueryLookupStrategy createSoftDeleteQueryLookupStrategy(
            QueryLookupStrategy strategy) {
        return new SoftDeleteReactiveElasticsearchQueryLookupStrategy(strategy, elasticsearchOperations);
    }
}