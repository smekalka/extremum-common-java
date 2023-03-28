package io.extremum.watch.controller;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.mapper.MapperDependencies;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.watch.dto.converter.TextWatchEventConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author rpuch
 */
@Configuration
@AutoConfigureWebMvc
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
public class WatchControllersTestConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        return new SystemJsonObjectMapper(Mockito.mock(MapperDependencies.class));
    }

    @Bean
    public TextWatchEventConverter textWatchEventConverter() {
        return new TextWatchEventConverter();
    }

    @Bean
    public DescriptorFactory descriptorFactory() {
        return new DescriptorFactory();
    }
}
