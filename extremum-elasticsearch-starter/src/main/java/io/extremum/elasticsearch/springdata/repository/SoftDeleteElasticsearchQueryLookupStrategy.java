package io.extremum.elasticsearch.springdata.repository;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import io.extremum.elasticsearch.SoftDeletion;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.repository.query.ElasticsearchPartQuery;
import org.springframework.data.elasticsearch.repository.query.ElasticsearchQueryMethod;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;

/**
 * @author rpuch
 */
public class SoftDeleteElasticsearchQueryLookupStrategy implements QueryLookupStrategy {
    private final QueryLookupStrategy strategy;
    private final ElasticsearchOperations elasticsearchOperations;
    private final SoftDeletion softDeletion = new SoftDeletion();

    public SoftDeleteElasticsearchQueryLookupStrategy(QueryLookupStrategy strategy,
            ElasticsearchOperations elasticsearchOperations) {
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

        if (!(repositoryQuery instanceof ElasticsearchPartQuery)) {
            return repositoryQuery;
        }
        ElasticsearchPartQuery partTreeQuery = (ElasticsearchPartQuery) repositoryQuery;

        return new SoftDeletePartTreeElasticsearchQuery(partTreeQuery);
    }

    private class SoftDeletePartTreeElasticsearchQuery extends ElasticsearchPartQuery {
        SoftDeletePartTreeElasticsearchQuery(ElasticsearchPartQuery partTreeQuery) {
            super((ElasticsearchQueryMethod) partTreeQuery.getQueryMethod(),
                    SoftDeleteElasticsearchQueryLookupStrategy.this.elasticsearchOperations);
        }

        @Override
        public CriteriaQuery createQuery(ParametersParameterAccessor accessor) {
            CriteriaQuery query = super.createQuery(accessor);
            return withNotDeleted(query);
        }

        private CriteriaQuery withNotDeleted(CriteriaQuery query) {
            return query.addCriteria(softDeletion.notDeleted());
        }
    }
}
