package io.extremum.elasticsearch.springdata.reactiverepository;

import io.extremum.common.repository.ExpressionsSupportingRepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.elasticsearch.repository.config.ReactiveElasticsearchRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * @author rpuch
 */
public class ExtremumReactiveElasticsearchRepositoriesRegistrar
		extends ExpressionsSupportingRepositoryBeanDefinitionRegistrarSupport {

   	@Override
   	protected Class<? extends Annotation> getAnnotation() {
   		return EnableExtremumReactiveElasticsearchRepositories.class;
   	}

   	@Override
   	protected RepositoryConfigurationExtension getExtension() {
   		return new ReactiveElasticsearchRepositoryConfigurationExtension();
   	}

}
