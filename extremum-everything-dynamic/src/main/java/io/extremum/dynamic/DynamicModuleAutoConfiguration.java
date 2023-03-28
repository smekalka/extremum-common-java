package io.extremum.dynamic;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.dynamic.config.DynamicModelGithubSchemaLocationConfiguration;
import io.extremum.dynamic.config.DynamicModelLocalSchemaLocationConfiguration;
import io.extremum.dynamic.config.DynamicModelProperties;
import io.extremum.dynamic.dao.JsonDynamicModelDao;
import io.extremum.dynamic.dao.MongoVersionedDynamicModelDao;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.services.DateTypesNormalizer;
import io.extremum.dynamic.services.DatesProcessor;
import io.extremum.dynamic.services.impl.DefaultDateTypesNormalizer;
import io.extremum.dynamic.services.impl.DefaultDatesProcessor;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import io.extremum.dynamic.validator.services.impl.networknt.NetworkntJsonDynamicModelValidator;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.watch.processor.ReactiveWatchEventConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Mono;

@Import({
        DynamicModelLocalSchemaLocationConfiguration.class,
        DynamicModelGithubSchemaLocationConfiguration.class,
})
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "dynamic-models", value = "schema.pointer.schema-name")
@EnableConfigurationProperties({DynamicModelProperties.class})
@ComponentScan(basePackages = "io.extremum.dynamic")
public class DynamicModuleAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SchemaMetaService schemaMetaService() {
        return new DefaultSchemaMetaService();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonDynamicModelValidator jsonDynamicModelValidator(NetworkntSchemaProvider provider,
                                                               ObjectMapper mapper, SchemaMetaService schemaMetaService) {
        return new NetworkntJsonDynamicModelValidator(provider, mapper, schemaMetaService);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveDescriptorDeterminator descriptorDeterminator(SchemaMetaService schemaMetaService) {
        return new DefaultReactiveDescriptorDeterminator(schemaMetaService);
    }

    @Bean
    @ConditionalOnMissingBean
    public DateTypesNormalizer dateDocumentTypesNormalizer() {
        return new DefaultDateTypesNormalizer();
    }

    @Bean
    @ConditionalOnMissingBean
    public DatesProcessor datesProcessor() {
        return new DefaultDatesProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonDynamicModelDao jsonDynamicModelDao(ReactiveMongoOperations ops, ReactiveMongoDescriptorFacilities facilities) {
        return new MongoVersionedDynamicModelDao(ops, facilities);
    }

    @Bean
    @ConditionalOnMissingBean
    ReactiveWatchEventConsumer dummyReactiveWatchEventConsumer() {
        return event -> Mono.empty();
    }

    @Bean
    @ConditionalOnMissingBean
    JsonDynamicModelObjectMapperBeanPostProcessor jsonDynamicModelObjectMapperBeanPostProcessor(){
        return new JsonDynamicModelObjectMapperBeanPostProcessor();
    }
}
