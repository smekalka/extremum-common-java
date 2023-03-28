package io.extremum.elasticsearch.dao;

import io.extremum.elasticsearch.config.ElasticsearchRepositoriesConfiguration;
import io.extremum.starter.CommonConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, ElasticsearchRepositoriesConfiguration.class})
public class ElasticsearchTestConfiguration {
}
