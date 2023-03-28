package io.extremum.jpa.dao;

import io.extremum.jpa.config.JpaConfiguration;
import io.extremum.jpa.properties.JpaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

@Configuration
@Import(JpaConfiguration.class)
@EnableConfigurationProperties(JpaProperties.class)
@RequiredArgsConstructor
public class JpaCommonDaoConfiguration {

    private final JpaProperties jpaProperties;

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(jpaProperties.getUri())
                .username(jpaProperties.getUsername())
                .password(jpaProperties.getPassword())
                .build();
    }
}
