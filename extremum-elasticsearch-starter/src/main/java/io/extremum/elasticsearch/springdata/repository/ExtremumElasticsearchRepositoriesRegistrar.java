package io.extremum.elasticsearch.springdata.repository;

import io.extremum.common.repository.ExpressionsSupportingRepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.elasticsearch.repository.config.ElasticsearchRepositoryConfigExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * @author rpuch
 */
public class ExtremumElasticsearchRepositoriesRegistrar
		extends ExpressionsSupportingRepositoryBeanDefinitionRegistrarSupport {

   	@Override
   	protected Class<? extends Annotation> getAnnotation() {
   		return EnableExtremumElasticsearchRepositories.class;
   	}

   	@Override
   	protected RepositoryConfigurationExtension getExtension() {
   		return new ElasticsearchRepositoryConfigExtension();
   	}

}
