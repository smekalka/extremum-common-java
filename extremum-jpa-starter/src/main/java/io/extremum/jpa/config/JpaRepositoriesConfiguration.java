package io.extremum.jpa.config;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.impl.DescriptorIdResolver;
import io.extremum.descriptors.sync.dao.impl.JpaDescriptorRepository;
import io.extremum.jpa.facilities.PostgresDescriptorFacilities;
import io.extremum.jpa.facilities.PostgresDescriptorFacilitiesAccessorConfigurator;
import io.extremum.jpa.facilities.PostgresDescriptorFacilitiesImpl;
import io.extremum.jpa.mapper.HibernateObjectMapper;
import io.extremum.jpa.properties.JpaProperties;
import io.extremum.jpa.repository.EnableExtremumJpaRepositories;
import io.extremum.jpa.repository.ExtremumJpaRepositoryFactoryBean;
import io.extremum.jpa.service.lifecycle.JpaCommonModelLifecycleListener;
import io.extremum.jpa.tx.JpaCollectionTransactor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.ArrayList;

/**
 * @author rpuch
 */
@Configuration
@ConditionalOnProperty("jpa.repository-packages")
@EnableConfigurationProperties(JpaProperties.class)
@EnableExtremumJpaRepositories(basePackages = "${jpa.repository-packages}",
        basePackageClasses = JpaDescriptorRepository.class,
        repositoryFactoryBeanClass = ExtremumJpaRepositoryFactoryBean.class)
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider", auditorAwareRef = "auditorProvider")
@RequiredArgsConstructor
public class JpaRepositoriesConfiguration {
    private final JpaProperties jpaProperties;

    @Bean
    @ConditionalOnMissingBean
    public PostgresDescriptorFacilities postgresqlDescriptorFacilities(DescriptorFactory descriptorFactory,
                                                                       DescriptorSaver descriptorSaver, DescriptorIdResolver resolver) {
        return new PostgresDescriptorFacilitiesImpl(descriptorFactory, descriptorSaver, resolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgresDescriptorFacilitiesAccessorConfigurator postgresqlDescriptorFacilitiesAccessorConfigurator(
            PostgresDescriptorFacilities postgresDescriptorFacilities) {
        return new PostgresDescriptorFacilitiesAccessorConfigurator(postgresDescriptorFacilities);
    }

    @Bean
    @ConditionalOnMissingBean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(jpaProperties.isGenerateDdl());
        vendorAdapter.setShowSql(jpaProperties.isShowSql());

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);

        ArrayList<String> packages = new ArrayList<>(jpaProperties.getEntityPackages());
        packages.add(Descriptor.class.getPackage().getName());
        factory.setPackagesToScan(packages.toArray(new String[0]));
        factory.setJpaPropertyMap(jpaProperties.getAdditional());

        factory.setDataSource(dataSource);
        return factory;
    }

    @Bean
    @ConditionalOnProperty(prefix = "extremum", name = "transaction.enabled", havingValue = "false", matchIfMissing = true)
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionOperations jpaTransactionOperations(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public JpaCollectionTransactor jpaCollectionTransactor(
            @Qualifier("jpaTransactionOperations") TransactionOperations transactionOperations) {
        return new JpaCollectionTransactor(transactionOperations);
    }

    @Bean
    @ConditionalOnMissingBean
    public JpaCommonModelLifecycleListener jpaCommonModelLifecycleListener() {
        return new JpaCommonModelLifecycleListener();
    }

    @Bean("hibernateObjectMapper")
    public HibernateObjectMapper hibernateObjectMapper() {
        return new HibernateObjectMapper();
    }
}