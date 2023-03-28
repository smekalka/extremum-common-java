package io.extremum.elasticsearch.springdata.reactiverepository;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.data.elasticsearch.repository.support.ReactiveElasticsearchRepositoryFactory;
import org.springframework.data.elasticsearch.repository.support.ReactiveElasticsearchRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * Factory bean for {@link ReactiveElasticsearchRepositoryFactory} extension that chooses automatically
 * whether to use the usual 'hard-delete' logic or 'soft-delete' logic.
 * For 'soft-delete' flavor, the repository makes all automagical
 * queries generated from query methods like <code>Person findByEmail(String email)</code>
 * respect the 'deleted' flag (unless annotated with @{@link SeesSoftlyDeletedRecords )}.
 *
 * It is to be referenced in &#064;{@link EnableReactiveElasticsearchRepositories}
 * like this:
 *
 * <pre>
 * &#064;EnableElasticsearchRepositories(repositoryFactoryBeanClass = SoftDeleteElasticsearchRepositoryFactoryBean.class,
        basePackages = "com.cybernation.testservice.repositories")
 * </pre>
 *
 * @author rpuch
 */
public class ExtremumReactiveElasticsearchRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends ReactiveElasticsearchRepositoryFactoryBean<T, S, ID> {
    private final Class<? extends T> repositoryInterface;

    public ExtremumReactiveElasticsearchRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);

        this.repositoryInterface = repositoryInterface;
    }

    @Override
    protected RepositoryFactorySupport getFactoryInstance(ReactiveElasticsearchOperations operations) {
        return new ExtremumReactiveElasticsearchRepositoryFactory(repositoryInterface, operations);
    }
}