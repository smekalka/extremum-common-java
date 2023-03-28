package io.extremum.mongo.springdata.reactiverepository;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import io.extremum.common.utils.ModelUtils;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * {@link ReactiveMongoRepositoryFactory} extension that chooses automatically
 * whether to use the usual 'hard-delete' logic or 'soft-delete' logic.
 * For 'soft-delete' flavor, the repository makes all automagical
 * queries generated from query methods like <code>Person findByEmail(String email)</code>
 * respect the 'deleted' flag (unless annotated with &#064;{@link SeesSoftlyDeletedRecords}).
 *
 * Also, this repository factory wraps all query method invocations to make sure
 * that for every object returned inside a {@link Flux} or a {@link Mono},
 * an {@link AfterConvertEvent} will be emitted reactively.
 *
 * @author rpuch
 * @see SeesSoftlyDeletedRecords
 */
public class ExtremumReactiveMongoRepositoryFactory extends ReactiveMongoRepositoryFactory {
    private final Class<?> repositoryInterface;
    private final ReactiveLookupStrategies lookupStrategies;

    public ExtremumReactiveMongoRepositoryFactory(Class<?> repositoryInterface,
            ReactiveMongoOperations mongoOperations) {
        super(mongoOperations);
        this.repositoryInterface = repositoryInterface;
        lookupStrategies = new ReactiveLookupStrategies(mongoOperations);
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable QueryLookupStrategy.Key key,
            QueryMethodEvaluationContextProvider evaluationContextProvider) {
        if (isSoftDelete()) {
            Optional<QueryLookupStrategy> optStrategy = super.getQueryLookupStrategy(key, evaluationContextProvider);
            return lookupStrategies.softDeleteQueryLookupStrategy(optStrategy, evaluationContextProvider);
        } else {
            return super.getQueryLookupStrategy(key, evaluationContextProvider);
        }
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        if (isSoftDelete()) {
            return SoftDeleteReactiveMongoRepository.class;
        } else {
            return HardDeleteReactiveMongoRepository.class;
        }
    }

    private boolean isSoftDelete() {
        RepositoryMetadata metadata = getRepositoryMetadata(repositoryInterface);
        return ModelUtils.isSoftDeletable(metadata.getDomainType());
    }

}
