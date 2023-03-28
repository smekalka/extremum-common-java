package io.extremum.mongo.springdata.reactiverepository;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import io.extremum.mongo.SoftDeletion;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.ConvertingParameterAccessor;
import org.springframework.data.mongodb.repository.query.ReactiveMongoQueryMethod;
import org.springframework.data.mongodb.repository.query.ReactivePartTreeMongoQuery;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.ReactiveQueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

/**
 * @author rpuch
 */
public class SoftDeleteReactiveMongoQueryLookupStrategy implements QueryLookupStrategy {
    private final QueryLookupStrategy strategy;
    private final ReactiveMongoOperations mongoOperations;
    private final QueryMethodEvaluationContextProvider evaluationContextProvider;
    private final SoftDeletion softDeletion = new SoftDeletion();

    private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    public SoftDeleteReactiveMongoQueryLookupStrategy(QueryLookupStrategy strategy,
            ReactiveMongoOperations mongoOperations, QueryMethodEvaluationContextProvider evaluationContextProvider) {
        this.strategy = strategy;
        this.mongoOperations = mongoOperations;
        this.evaluationContextProvider = evaluationContextProvider;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
            NamedQueries namedQueries) {
        RepositoryQuery repositoryQuery = strategy.resolveQuery(method, metadata, factory, namedQueries);

        if (method.getAnnotation(SeesSoftlyDeletedRecords.class) != null) {
            return repositoryQuery;
        }

        if (!(repositoryQuery instanceof ReactivePartTreeMongoQuery)) {
            return repositoryQuery;
        }
        ReactivePartTreeMongoQuery partTreeQuery = (ReactivePartTreeMongoQuery) repositoryQuery;

        return new SoftDeleteReactivePartTreeMongoQuery(partTreeQuery);
    }

    private class SoftDeleteReactivePartTreeMongoQuery extends ReactivePartTreeMongoQuery {
        SoftDeleteReactivePartTreeMongoQuery(ReactivePartTreeMongoQuery partTreeQuery) {
            super((ReactiveMongoQueryMethod) partTreeQuery.getQueryMethod(), mongoOperations,
                    EXPRESSION_PARSER, (ReactiveQueryMethodEvaluationContextProvider) evaluationContextProvider);
        }

        @Override
        protected Mono<Query> createQuery(ConvertingParameterAccessor accessor) {
            Mono<Query> query = super.createQuery(accessor);
            return withNotDeleted(query);
        }

        @Override
        protected Mono<Query> createCountQuery(ConvertingParameterAccessor accessor) {
            Mono<Query> query = super.createCountQuery(accessor);
            return withNotDeleted(query);
        }
        
        private Mono<Query> withNotDeleted(Mono<Query> query) {
           query.map(query1 -> query1.addCriteria(softDeletion.notDeleted())).subscribe();
           return query;
        }
    }
}
