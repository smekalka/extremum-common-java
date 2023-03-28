package io.extremum.mongo.config;

import com.mongodb.WriteConcern;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.descriptor.factory.impl.ReactiveDescriptorIdResolver;
import io.extremum.mongo.dbfactory.MainMongoDb;
import io.extremum.mongo.dbfactory.reactive.ReactiveMongoDbFactoryConfiguration;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilitiesImpl;
import io.extremum.mongo.service.lifecycle.ReactiveMongoCommonModelLifecycleCallbacks;
import io.extremum.mongo.service.lifecycle.ReactiveMongoVersionedModelLifecycleCallbacks;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
@Import(ReactiveMongoDbFactoryConfiguration.class)
@ConditionalOnProperty(prefix = "mongo", value = "uri")
public class MainReactiveMongoConfiguration {
    @Bean
    @Primary
    @MainMongoDb
    public ReactiveMongoOperations reactiveMongoTemplate(ReactiveMongoDatabaseFactory mongoDatabaseFactory,
                                                         MappingMongoConverter mappingMongoConverter) {
        ReactiveMongoTemplate template = new ReactiveMongoTemplate(mongoDatabaseFactory, mappingMongoConverter);
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        template.setWriteConcern(WriteConcern.MAJORITY);
        return template;
    }

    @Bean
    @MainMongoDb
    public ReactiveMongoTransactionManager reactiveMongoTransactionManager(
            ReactiveMongoDatabaseFactory mongoDatabaseFactory) {
        return new ReactiveMongoTransactionManager(mongoDatabaseFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities(
            DescriptorFactory descriptorFactory, ReactiveDescriptorSaver reactiveDescriptorSaver,
            ReactiveDescriptorIdResolver descriptorIdResolver) {
        return new ReactiveMongoDescriptorFacilitiesImpl(descriptorFactory, reactiveDescriptorSaver, descriptorIdResolver);
    }

    @Bean
    public ReactiveMongoCommonModelLifecycleCallbacks reactiveMongoCommonModelLifecycleCallbacks(
            ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities) {
        return new ReactiveMongoCommonModelLifecycleCallbacks(reactiveMongoDescriptorFacilities);
    }

    @Bean
    public ReactiveMongoVersionedModelLifecycleCallbacks reactiveMongoVersionedModelLifecycleCallbacks(
            ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities) {
        return new ReactiveMongoVersionedModelLifecycleCallbacks(reactiveMongoDescriptorFacilities);
    }
}
