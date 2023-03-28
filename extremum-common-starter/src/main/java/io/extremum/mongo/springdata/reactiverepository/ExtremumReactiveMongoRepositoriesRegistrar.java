package io.extremum.mongo.springdata.reactiverepository;

import io.extremum.common.repository.ExpressionsSupportingRepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.mongodb.repository.config.ReactiveMongoRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * @author rpuch
 */
public class ExtremumReactiveMongoRepositoriesRegistrar
		extends ExpressionsSupportingRepositoryBeanDefinitionRegistrarSupport {

   	@Override
   	protected Class<? extends Annotation> getAnnotation() {
   		return EnableExtremumReactiveMongoRepositories.class;
   	}

   	@Override
   	protected RepositoryConfigurationExtension getExtension() {
   		return new ReactiveMongoRepositoryConfigurationExtension();
   	}

}
