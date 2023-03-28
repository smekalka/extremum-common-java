package io.extremum.everything.regular.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.test.core.MockedMapperDependencies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EverythingControllersTestConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        return new SystemJsonObjectMapper(new MockedMapperDependencies());
    }
}
