package io.extremum.security.config;

import io.extremum.security.model.jwt.OidcJwtTokenConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtTokenConfiguration {

    final ApplicationContext applicationContext;

    public JwtTokenConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public OidcJwtTokenConverter oidcJwtTokenConverter() {
        return new OidcJwtTokenConverter(applicationContext.getId());
    }
}