package io.extremum.descriptors.common;

import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.mapping.model.CamelCaseAbbreviatingFieldNamingStrategy;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mongodb.config.MongoConfigurationSupport;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singletonList;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
@Configuration
public class CommonDescriptorsMongoConfiguration {
    private final MongoCustomConversions customConversions;

    @Bean
    @DescriptorsMongoDb
    public MappingMongoConverter descriptorsMappingMongoConverter()
            throws Exception {

        // We do not allow DBRefs because they are pain (at least I was told so)
        DbRefResolver dbRefResolver = NoOpDbRefResolver.INSTANCE;
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, descriptorsMongoMappingContext());
        converter.setCustomConversions(customConversions);
        converter.setEntityCallbacks(explicitCallbacksToAvoidCircularDependencyProblem());

        return converter;
    }

    private EntityCallbacks explicitCallbacksToAvoidCircularDependencyProblem() {
        // we have to construct this explicitly because otherwise any EntityCallback that depends on descriptor-related
        // beans (like the ones that support CommonModel descriptor-related filling/resolving) would create an
        // unresolvable circular dependency during startup
        return EntityCallbacks.create();
    }

    @Bean
    public MongoMappingContext descriptorsMongoMappingContext() throws ClassNotFoundException {
        CustomDescriptorCollectionMappingContext mappingContext = new CustomDescriptorCollectionMappingContext();
        mappingContext.setInitialEntitySet(getInitialEntitySet());
        mappingContext.setSimpleTypeHolder(customConversions.getSimpleTypeHolder());
        mappingContext.setFieldNamingStrategy(fieldNamingStrategy());
        mappingContext.setAutoIndexCreation(true);
        return mappingContext;
    }

    private Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {

        Set<Class<?>> initialEntitySet = new HashSet<>();

        for (String basePackage : getMappingBasePackages()) {
            initialEntitySet.addAll(scanForEntities(basePackage));
        }

        return initialEntitySet;
    }

    private Collection<String> getMappingBasePackages() {
        return singletonList(Descriptor.class.getPackage().getName());
    }

    private Set<Class<?>> scanForEntities(String basePackage) throws ClassNotFoundException {

        if (!StringUtils.hasText(basePackage)) {
            return Collections.emptySet();
        }

        Set<Class<?>> initialEntitySet = new HashSet<>();

        if (StringUtils.hasText(basePackage)) {

            ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(
                    false);
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));

            for (BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {

                initialEntitySet
                        .add(ClassUtils.forName(candidate.getBeanClassName(),
                                MongoConfigurationSupport.class.getClassLoader()));
            }
        }

        // adding it explicitly because otherwise it won't be added
        initialEntitySet.add(Descriptor.class);

        return initialEntitySet;
    }

    private boolean abbreviateFieldNames() {
        return false;
    }

    private FieldNamingStrategy fieldNamingStrategy() {
        return abbreviateFieldNames() ? new CamelCaseAbbreviatingFieldNamingStrategy()
                : PropertyNameFieldNamingStrategy.INSTANCE;
    }
}
