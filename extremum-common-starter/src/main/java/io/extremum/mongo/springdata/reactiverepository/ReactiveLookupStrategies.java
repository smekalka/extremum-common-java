package io.extremum.mongo.springdata.reactiverepository;

import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

import java.util.Optional;

/**
 * @author rpuch
 */
class ReactiveLookupStrategies {
    private final ReactiveMongoOperations mongoOperations;

    ReactiveLookupStrategies(ReactiveMongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<QueryLookupStrategy> softDeleteQueryLookupStrategy(Optional<QueryLookupStrategy> optStrategy,
            QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return optStrategy.map(strategy
                -> createSoftDeleteQueryLookupStrategy(strategy, evaluationContextProvider));
    }

    private QueryLookupStrategy createSoftDeleteQueryLookupStrategy(QueryLookupStrategy strategy,
            QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return new SoftDeleteReactiveMongoQueryLookupStrategy(strategy, mongoOperations, evaluationContextProvider);
    }
}
