package io.extremum.elasticsearch.springdata.repository;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import io.extremum.common.utils.ModelUtils;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

import java.util.Optional;

/**
 * {@link ElasticsearchRepositoryFactory} extension that chooses automatically
 * whether to use the usual 'hard-delete' logic or 'soft-delete' logic.
 * For 'soft-delete' flavor, the repository makes all automagical
 * queries generated from query methods like <code>Person findByEmail(String email)</code>
 * respect the 'deleted' flag (unless annotated with &#064;{@link SeesSoftlyDeletedRecords}).
 *
 * @author rpuch
 * @see SeesSoftlyDeletedRecords
 */
public class ExtremumElasticsearchRepositoryFactory extends ElasticsearchRepositoryFactory {
    private final Class<?> repositoryInterface;
    private final LookupStrategies lookupStrategies;

    public ExtremumElasticsearchRepositoryFactory(Class<?> repositoryInterface,
            ElasticsearchOperations elasticsearchOperations) {
        super(elasticsearchOperations);
        this.repositoryInterface = repositoryInterface;
        lookupStrategies = new LookupStrategies(elasticsearchOperations);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        if (isSoftDelete()) {
            return SoftDeleteElasticsearchRepository.class;
        } else {
            return HardDeleteElasticsearchRepository.class;
        }
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(QueryLookupStrategy.Key key,
            QueryMethodEvaluationContextProvider evaluationContextProvider) {
        if (isSoftDelete()) {
            Optional<QueryLookupStrategy> optStrategy = super.getQueryLookupStrategy(key, evaluationContextProvider);
            return lookupStrategies.softDeleteQueryLookupStrategy(optStrategy);
        } else {
            return super.getQueryLookupStrategy(key, evaluationContextProvider);
        }
    }

    private boolean isSoftDelete() {
        RepositoryMetadata metadata = getRepositoryMetadata(repositoryInterface);
        return ModelUtils.isSoftDeletable(metadata.getDomainType());
    }
}
