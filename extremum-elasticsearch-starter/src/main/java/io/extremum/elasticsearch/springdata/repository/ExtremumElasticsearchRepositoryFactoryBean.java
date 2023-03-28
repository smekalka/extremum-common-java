package io.extremum.elasticsearch.springdata.repository;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactory;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * Factory bean for {@link ElasticsearchRepositoryFactory} extension that chooses automatically
 * whether to use the usual 'hard-delete' logic or 'soft-delete' logic.
 * For 'soft-delete' flavor, the repository makes all automagical
 * queries generated from query methods like <code>Person findByEmail(String email)</code>
 * respect the 'deleted' flag (unless annotated with @{@link SeesSoftlyDeletedRecords )}.
 *
 * It is to be referenced in &#064;{@link EnableElasticsearchRepositories}
 * like this:
 *
 * <pre>
 * &#064;EnableElasticsearchRepositories(repositoryFactoryBeanClass = SoftDeleteElasticsearchRepositoryFactoryBean.class,
        basePackages = "com.cybernation.testservice.repositories")
 * </pre>
 *
 * @author rpuch
 */
public class ExtremumElasticsearchRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends ElasticsearchRepositoryFactoryBean<T, S, ID> {
    private final Class<? extends T> repositoryInterface;
    private ElasticsearchOperations operations;

    public ExtremumElasticsearchRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);

        this.repositoryInterface = repositoryInterface;
    }

    @Override
    public void setElasticsearchOperations(ElasticsearchOperations operations) {
        super.setElasticsearchOperations(operations);
        this.operations = operations;
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        return new ExtremumElasticsearchRepositoryFactory(repositoryInterface, operations);
    }
}