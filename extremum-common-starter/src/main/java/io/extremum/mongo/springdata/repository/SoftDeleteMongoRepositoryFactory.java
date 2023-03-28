package io.extremum.mongo.springdata.repository;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

import java.util.Optional;

/**
 * {@link MongoRepositoryFactory} extension that makes all automagical
 * queries generated from query methods like <code>Person findByEmail(String email)</code>
 * respect the 'deleted' flag (unless annotated with &#064;{@link SeesSoftlyDeletedRecords}).
 *
 * @author rpuch
 * @see SeesSoftlyDeletedRecords
 */
public class SoftDeleteMongoRepositoryFactory extends MongoRepositoryFactory {
    private final LookupStrategies lookupStrategies;

    public SoftDeleteMongoRepositoryFactory(MongoOperations mongoOperations) {
        super(mongoOperations);
        lookupStrategies = new LookupStrategies(mongoOperations);
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(QueryLookupStrategy.Key key,
            QueryMethodEvaluationContextProvider evaluationContextProvider) {
        Optional<QueryLookupStrategy> optStrategy = super.getQueryLookupStrategy(key, evaluationContextProvider);
        return lookupStrategies.softDeleteQueryLookupStrategy(optStrategy, evaluationContextProvider);
    }

}
