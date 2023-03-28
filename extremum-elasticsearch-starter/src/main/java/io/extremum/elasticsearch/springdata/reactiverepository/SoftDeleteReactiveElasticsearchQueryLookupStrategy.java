package io.extremum.elasticsearch.springdata.reactiverepository;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import io.extremum.elasticsearch.SoftDeletion;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.query.ElasticsearchParameterAccessor;
import org.springframework.data.elasticsearch.repository.query.ReactiveElasticsearchQueryMethod;
import org.springframework.data.elasticsearch.repository.query.ReactivePartTreeElasticsearchQuery;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;

/**
 * @author rpuch
 */
public class SoftDeleteReactiveElasticsearchQueryLookupStrategy implements QueryLookupStrategy {
    private final QueryLookupStrategy strategy;
    private final ReactiveElasticsearchOperations elasticsearchOperations;
    private final SoftDeletion softDeletion = new SoftDeletion();

    public SoftDeleteReactiveElasticsearchQueryLookupStrategy(QueryLookupStrategy strategy,
                                                              ReactiveElasticsearchOperations elasticsearchOperations) {
        this.strategy = strategy;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
            NamedQueries namedQueries) {
        RepositoryQuery repositoryQuery = strategy.resolveQuery(method, metadata, factory, namedQueries);

        if (method.getAnnotation(SeesSoftlyDeletedRecords.class) != null) {
            return repositoryQuery;
        }

        if (!(repositoryQuery instanceof ReactivePartTreeElasticsearchQuery)) {
            return repositoryQuery;
        }
        ReactivePartTreeElasticsearchQuery partTreeQuery = (ReactivePartTreeElasticsearchQuery) repositoryQuery;

        return new SoftDeleteReactivePartTreeElasticsearchQuery(partTreeQuery);
    }

    private class SoftDeleteReactivePartTreeElasticsearchQuery extends ReactivePartTreeElasticsearchQuery {
        SoftDeleteReactivePartTreeElasticsearchQuery(ReactivePartTreeElasticsearchQuery partTreeQuery) {
            super((ReactiveElasticsearchQueryMethod) partTreeQuery.getQueryMethod(),
                    SoftDeleteReactiveElasticsearchQueryLookupStrategy.this.elasticsearchOperations);
        }

        @Override
        public CriteriaQuery createQuery(ElasticsearchParameterAccessor accessor) {
            Query query = super.createQuery(accessor);
            if (query instanceof CriteriaQuery) {
                CriteriaQuery criteriaQuery = (CriteriaQuery) query;
                return withNotDeleted(criteriaQuery);
            }

            throw new IllegalStateException(
                    "Only CriteriaQuery instances are supported but was given " + query.getClass());
        }

        private CriteriaQuery withNotDeleted(CriteriaQuery query) {
            return query.addCriteria(softDeletion.notDeleted());
        }
    }
}
