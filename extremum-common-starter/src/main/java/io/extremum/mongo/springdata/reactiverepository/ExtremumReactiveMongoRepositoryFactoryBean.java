package io.extremum.mongo.springdata.reactiverepository;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory;
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * Factory bean for {@link ReactiveMongoRepositoryFactory} extension that chooses automatically
 * whether to use the usual 'hard-delete' logic or 'soft-delete' logic.
 * For 'soft-delete' flavor, the repository makes all automagical
 * queries generated from query methods like <code>Person findByEmail(String email)</code>
 * respect the 'deleted' flag (unless annotated with @{@link SeesSoftlyDeletedRecords )}.
 *
 * It is to be referenced in &#064;{@link EnableReactiveMongoRepositories}
 * like this:
 *
 * <pre>
 * &#064;EnableReactiveMongoRepositories(repositoryFactoryBeanClass = SoftDeleteReactiveMongoRepositoryFactoryBean.class,
        basePackages = "com.cybernation.testservice.repositories")
 * </pre>
 *
 * @author rpuch
 */
public class ExtremumReactiveMongoRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends ReactiveMongoRepositoryFactoryBean<T, S, ID> {
    private final Class<? extends T> repositoryInterface;

    public ExtremumReactiveMongoRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
        this.repositoryInterface = repositoryInterface;
    }

    @Override
    protected RepositoryFactorySupport getFactoryInstance(ReactiveMongoOperations operations) {
        return new ExtremumReactiveMongoRepositoryFactory(repositoryInterface, operations);
    }
}