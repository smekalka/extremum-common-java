package io.extremum.mongo.springdata.repository;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * Factory bean for {@link MongoRepositoryFactory} extension that chooses automatically
 * whether to use the usual 'hard-delete' logic or 'soft-delete' logic.
 * For 'soft-delete' flavor, the repository makes all automagical
 * queries generated from query methods like <code>Person findByEmail(String email)</code>
 * respect the 'deleted' flag (unless annotated with @{@link SeesSoftlyDeletedRecords )}.
 *
 * It is to be referenced in &#064;{@link EnableMongoRepositories}
 * like this:
 *
 * <pre>
 * &#064;EnableMongoRepositories(repositoryFactoryBeanClass = SoftDeleteMongoRepositoryFactoryBean.class,
        basePackages = "com.cybernation.testservice.repositories")
 * </pre>
 *
 * @author rpuch
 */
public class ExtremumMongoRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends MongoRepositoryFactoryBean<T, S, ID> {
    private final Class<? extends T> repositoryInterface;

    public ExtremumMongoRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
        this.repositoryInterface = repositoryInterface;
    }

    @Override
    protected RepositoryFactorySupport getFactoryInstance(MongoOperations operations) {
        return new ExtremumMongoRepositoryFactory(repositoryInterface, operations);
    }
}