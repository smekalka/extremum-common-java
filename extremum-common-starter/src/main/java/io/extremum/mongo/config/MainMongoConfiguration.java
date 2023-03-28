package io.extremum.mongo.config;

import com.mongodb.WriteConcern;
import io.extremum.common.annotation.SecondaryDatasource;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.impl.DescriptorIdResolver;
import io.extremum.descriptors.common.StumpFacilities;
import io.extremum.mongo.dbfactory.MainMongoDb;
import io.extremum.mongo.dbfactory.sync.MongoDbFactoryConfiguration;
import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.mongo.facilities.MongoDescriptorFacilitiesImpl;
import io.extremum.mongo.properties.MongoProperties;
import io.extremum.mongo.service.lifecycle.MongoCommonModelLifecycleCallbacks;
import io.extremum.mongo.springdata.EnableAllMongoAuditing;
import io.extremum.starter.DateToZonedDateTimeConverter;
import io.extremum.starter.DescriptorToStringConverter;
import io.extremum.starter.ZonedDateTimeToDateConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.convert.MappingContextTypeInformationMapper;
import org.springframework.data.mapping.model.CamelCaseAbbreviatingFieldNamingStrategy;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.MongoConfigurationSupport;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * @author rpuch
 */
@Configuration
@EnableConfigurationProperties(MongoProperties.class)
@EnableAllMongoAuditing(dateTimeProviderRef = "dateTimeProvider")
@RequiredArgsConstructor
@Import(MongoDbFactoryConfiguration.class)
@ConditionalOnProperty(prefix = "mongo", value = "uri")
public class MainMongoConfiguration {
    private final MongoProperties mongoProperties;
    private final List<CustomMongoConvertersSupplier> customMongoConvertersSuppliers;

    @Bean
    @Primary
    @MainMongoDb
    public MongoTemplate mongoTemplate(MongoDatabaseFactory databaseFactory, MappingMongoConverter converter) {
        MongoTemplate template = new MongoTemplate(databaseFactory, converter);
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        template.setWriteConcern(WriteConcern.MAJORITY);
        return template;
    }

    @Bean
    @Primary
    @MainMongoDb
    public MongoMappingContext mongoMappingContext(MongoCustomConversions customConversions)
            throws ClassNotFoundException {
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setInitialEntitySet(getInitialEntitySet());
        mappingContext.setSimpleTypeHolder(customConversions.getSimpleTypeHolder());
        mappingContext.setFieldNamingStrategy(fieldNamingStrategy());
        mappingContext.setAutoIndexCreation(true);
        return mappingContext;
    }

    private FieldNamingStrategy fieldNamingStrategy() {
        return abbreviateFieldNames() ? new CamelCaseAbbreviatingFieldNamingStrategy()
                : PropertyNameFieldNamingStrategy.INSTANCE;
    }

    private boolean abbreviateFieldNames() {
        return false;
    }

    @Bean
    @Primary
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory databaseFactory,
    			MongoCustomConversions customConversions, MongoMappingContext mappingContext) {
        // TODO: enable DBRef resolving when we separate reactive from sync code
        DbRefResolver dbRefResolver = NoOpDbRefResolver.INSTANCE;
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mappingContext);
        converter.setCustomConversions(customConversions);
        converter.setCodecRegistryProvider(databaseFactory);

        // changing type mapper so that:
        // 1. if there is no @TypeAlias on the model class, _class attribute is not saved
        // 2. if @TypeAlias is there, its value is saved in _class attribute
        MappingContextTypeInformationMapper typeInformationMapper = new MappingContextTypeInformationMapper(
                mappingContext);
        DefaultMongoTypeMapper typeMapper = new MongoTypeMapperWithSearchByExampleFix(typeInformationMapper);
        converter.setTypeMapper(typeMapper);

        return converter;
    }

    private Collection<String> getMappingBasePackages() {
        return mongoProperties.getModelPackages();
    }

    private Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {
        return getInitialEntitySet0().stream()
                .filter(this::isNotOnSecondaryDatasource)
                .collect(toSet());
    }

    private Set<Class<?>> getInitialEntitySet0() throws ClassNotFoundException {
        Set<Class<?>> initialEntitySet = new HashSet<>();

        for (String basePackage : getMappingBasePackages()) {
            initialEntitySet.addAll(this.scanForEntities(basePackage));
        }

        return initialEntitySet;
    }

    private Set<Class<?>> scanForEntities(String basePackage) throws ClassNotFoundException {
        if (!StringUtils.hasText(basePackage)) {
            return Collections.emptySet();
        } else {
            Set<Class<?>> initialEntitySet = new HashSet<>();
            if (StringUtils.hasText(basePackage)) {
                ClassPathScanningCandidateComponentProvider componentProvider
                        = new ClassPathScanningCandidateComponentProvider(false);
                componentProvider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
                componentProvider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));

                for (BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {
                    initialEntitySet.add(ClassUtils.forName(candidate.getBeanClassName(),
                            MongoConfigurationSupport.class.getClassLoader()));
                }
            }

            return initialEntitySet;
        }
    }

    private boolean isNotOnSecondaryDatasource(Class<?> entityClass) {
        return AnnotationUtils.findAnnotation(entityClass, SecondaryDatasource.class) == null;
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Object> converters = new ArrayList<>();

        converters.add(new DateToZonedDateTimeConverter());
        converters.add(new ZonedDateTimeToDateConverter());
        converters.add(new DescriptorToStringConverter());
        converters.add(new EnumToStringConverter());
        converters.add(new StringToEnumConverterFactory());

        customMongoConvertersSuppliers.forEach(supplier -> {
            converters.addAll(supplier.getConverters());
        });

        return new MongoCustomConversions(converters);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoDescriptorFacilities mongoDescriptorFacilities(DescriptorFactory descriptorFactory,
            DescriptorSaver descriptorSaver, DescriptorIdResolver descriptorIdResolver) {
        return new MongoDescriptorFacilitiesImpl(descriptorFactory, descriptorSaver, descriptorIdResolver);
    }

    @Bean
    public MongoCommonModelLifecycleCallbacks mongoCommonModelLifecycleCallbacks(
            MongoDescriptorFacilities mongoDescriptorFacilities, StumpFacilities stumpFacilities) {
        return new MongoCommonModelLifecycleCallbacks(mongoDescriptorFacilities, stumpFacilities);
    }

}
