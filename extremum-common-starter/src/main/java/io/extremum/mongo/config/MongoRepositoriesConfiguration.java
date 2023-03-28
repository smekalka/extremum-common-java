package io.extremum.mongo.config;

import io.extremum.common.secondaryds.ModelOnSecondaryDatasourceFilter;
import io.extremum.mongo.properties.MongoProperties;
import io.extremum.mongo.springdata.reactiverepository.EnableExtremumReactiveMongoRepositories;
import io.extremum.mongo.springdata.reactiverepository.ExtremumReactiveMongoRepositoryFactoryBean;
import io.extremum.mongo.springdata.repository.EnableExtremumMongoRepositories;
import io.extremum.mongo.springdata.repository.ExtremumMongoRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * @author rpuch
 */
@Configuration
@EnableExtremumMongoRepositories(basePackages = "${mongo.repository-packages}",
        repositoryFactoryBeanClass = ExtremumMongoRepositoryFactoryBean.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM,
                classes = ModelOnSecondaryDatasourceFilter.class))
@EnableExtremumReactiveMongoRepositories(basePackages = "${mongo.repository-packages}",
        repositoryFactoryBeanClass = ExtremumReactiveMongoRepositoryFactoryBean.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM,
                classes = ModelOnSecondaryDatasourceFilter.class))
@ConditionalOnProperty(MongoProperties.REPOSITORY_PACKAGES_PROPERTY)
public class MongoRepositoriesConfiguration {
}
