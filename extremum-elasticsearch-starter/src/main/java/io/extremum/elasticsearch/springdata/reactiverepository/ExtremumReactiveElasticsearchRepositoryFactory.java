package io.extremum.elasticsearch.springdata.reactiverepository;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import io.extremum.common.utils.ModelUtils;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.support.ReactiveElasticsearchRepositoryFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

import java.util.Optional;

/**
 * {@link ReactiveElasticsearchRepositoryFactory} extension that chooses automatically
 * whether to use the usual 'hard-delete' logic or 'soft-delete' logic.
 * For 'soft-delete' flavor, the repository makes all automagical
 * queries generated from query methods like <code>Person findByEmail(String email)</code>
 * respect the 'deleted' flag (unless annotated with &#064;{@link SeesSoftlyDeletedRecords}).
 *
 * @author rpuch
 * @see SeesSoftlyDeletedRecords
 */
public class ExtremumReactiveElasticsearchRepositoryFactory extends ReactiveElasticsearchRepositoryFactory {
    private final Class<?> repositoryInterface;
    private final ReactiveLookupStrategies lookupStrategies;

    public ExtremumReactiveElasticsearchRepositoryFactory(Class<?> repositoryInterface,
            ReactiveElasticsearchOperations elasticsearchOperations) {
        super(elasticsearchOperations);
        this.repositoryInterface = repositoryInterface;
        lookupStrategies = new ReactiveLookupStrategies(elasticsearchOperations);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        if (isSoftDelete()) {
            return SoftDeleteReactiveElasticsearchRepository.class;
        } else {
            return HardDeleteReactiveElasticsearchRepository.class;
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
