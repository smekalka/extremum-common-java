package io.extremum.elasticsearch.dao;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author rpuch
 */
@Configuration
@Import(ElasticsearchTestConfiguration.class)
public class RepositoryBasedElasticsearchDaoConfiguration {
}
