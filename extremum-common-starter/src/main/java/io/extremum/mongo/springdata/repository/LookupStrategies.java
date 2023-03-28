package io.extremum.mongo.springdata.repository;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

import java.util.Optional;

/**
 * @author rpuch
 */
class LookupStrategies {
    private final MongoOperations mongoOperations;

    LookupStrategies(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<QueryLookupStrategy> softDeleteQueryLookupStrategy(Optional<QueryLookupStrategy> optStrategy,
            QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return optStrategy.map(strategy -> createSoftDeleteQueryLookupStrategy(strategy, evaluationContextProvider));
    }

    private SoftDeleteMongoQueryLookupStrategy createSoftDeleteQueryLookupStrategy(QueryLookupStrategy strategy,
            QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return new SoftDeleteMongoQueryLookupStrategy(strategy, mongoOperations, evaluationContextProvider);
    }
}
