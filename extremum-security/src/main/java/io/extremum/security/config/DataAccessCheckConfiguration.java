package io.extremum.security.config;

import io.extremum.security.rules.config.DataAccessCheckerBeanFactory;
import io.extremum.security.rules.config.DataAccessCheckerSupportedModelProvider;
import io.extremum.security.rules.service.DataAccessCheckerFactory;
import io.extremum.security.rules.service.DefaultDataAccessCheckerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class DataAccessCheckConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataAccessCheckerFactory dataAccessCheckerFactory() {
        return new DefaultDataAccessCheckerFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public DataAccessCheckerSupportedModelProvider noModelsSupportedModelsProvider(){
        return Collections::emptyList;
    }

    @Bean
    @ConditionalOnMissingBean
    public DataAccessCheckerBeanFactory dataAccessCheckerBeanFactory(
            DataAccessCheckerSupportedModelProvider supportedModelProvider,
            DataAccessCheckerFactory dataAccessCheckerFactory
    ) {
        return new DataAccessCheckerBeanFactory(supportedModelProvider.getSupportedModels(), dataAccessCheckerFactory);
    }
}
