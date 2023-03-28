package io.extremum.elasticsearch.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.elasticsearch.facilities.ElasticsearchDescriptorFacilities;
import io.extremum.elasticsearch.properties.ElasticsearchProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author rpuch
 */
@Configuration
@Import(ElasticsearchTestConfiguration.class)
public class ClassicElasticsearchDaoConfiguration {
    @Bean
    public ClassicTestElasticsearchModelDao classicTestElasticsearchModelDao(
            ElasticsearchProperties elasticsearchProperties,
            DescriptorService descriptorService,
            ElasticsearchDescriptorFacilities descriptorFacilities,
            ObjectMapper objectMapper) {
        return new ClassicTestElasticsearchModelDao(elasticsearchProperties, descriptorService,
                descriptorFacilities, objectMapper);
    }
}
