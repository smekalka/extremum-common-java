package io.extremum.schema.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.everything.services.models.ModelSettingsProvider;
import io.extremum.schema.controller.ExtremumModelController;
import io.extremum.schema.service.ExtremumModelSchemaService;
import io.extremum.schema.service.FileSystemModelSettingsProvider;
import io.extremum.schema.service.ModelSchemaRegistrar;
import io.extremum.starter.properties.ModelProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

@Configuration
public class ExtremumModelConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExtremumModelSchemaService extremumModelService(
            @Value("${extremum.schema.path}") String schemaPath,
            ModelSchemaRegistrar modelSchemaRegistrar,
            ModelSettingsProvider modelSettingsProvider,
            ObjectMapper objectMapper) {
        return new ExtremumModelSchemaService(schemaPath, modelSchemaRegistrar, modelSettingsProvider, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FileSystemModelSettingsProvider modelSettingsProvider(@Qualifier("modelSettingsMessageChannel") MessageChannel modelSettingMessageChannel) {
        return new FileSystemModelSettingsProvider(modelSettingMessageChannel);
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ModelSchemaRegistrar modelSchemaRegistrar(ModelProperties modelProperties, ModelSettingsProvider modelSettingsProvider) {
        return new ModelSchemaRegistrar(modelProperties.getPackageNames(), modelSettingsProvider);
    }

    @Bean
    public ExtremumModelController extremumModelController(ExtremumModelSchemaService extremumModelService) {
        return new ExtremumModelController(extremumModelService);
    }
}
