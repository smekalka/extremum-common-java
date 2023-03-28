package io.extremum.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.codec.ServerSentEventWithErrorMessagesHttpMessageWriter;
import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.collection.service.CollectionDescriptorServiceImpl;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorExtractor;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorServiceImpl;
import io.extremum.common.collection.service.ReactiveCollectionOverride;
import io.extremum.common.collection.service.ReactiveCollectionOverridesWithDescriptorExtractorList;
import io.extremum.common.collection.service.ReactiveOwnedModelDescriptorService;
import io.extremum.common.collection.service.ReactiveOwnedModelDescriptorServiceImpl;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.descriptor.factory.impl.DescriptorIdResolver;
import io.extremum.common.descriptor.factory.impl.ReactiveDescriptorIdResolver;
import io.extremum.common.descriptor.serde.StringToDescriptorConverter;
import io.extremum.common.descriptor.service.DBDescriptorLoader;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.descriptor.service.DescriptorServiceImpl;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.descriptor.service.ReactiveDescriptorServiceImpl;
import io.extremum.common.descriptor.service.StaticDescriptorLoaderAccessorConfigurator;
import io.extremum.common.exceptions.DefaultExtremumExceptionHandlers;
import io.extremum.common.exceptions.ExtremumExceptionHandlers;
import io.extremum.common.exceptions.handler.DefaultExceptionHandler;
import io.extremum.common.exceptions.handler.annotation.AnnotationBasedExtremumExceptionResolver;
import io.extremum.common.exceptions.handler.annotation.DisabledExtremumExceptionResolver;
import io.extremum.common.exceptions.handler.annotation.ExtremumExceptionResolver;
import io.extremum.common.exceptions.properties.ExceptionProperties;
import io.extremum.common.iri.properties.IriProperties;
import io.extremum.common.iri.service.DefaultIriFacilities;
import io.extremum.common.iri.service.IriFacilities;
import io.extremum.common.mapper.MapperDependencies;
import io.extremum.common.mapper.MapperDependenciesImpl;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.common.model.advice.BasicModelComposingIriAspect;
import io.extremum.common.model.owned.OwnedModelLifecycleSupport;
import io.extremum.common.model.owned.advice.OwnedModelDescriptorSaverAspect;
import io.extremum.common.reactive.IsolatedSchedulerReactifier;
import io.extremum.common.reactive.Reactifier;
import io.extremum.common.response.status.HeaderBasedResponseStatusCodeResolver;
import io.extremum.common.response.status.ResponseStatusCodeResolver;
import io.extremum.common.service.CommonService;
import io.extremum.common.service.ReactiveCommonService;
import io.extremum.common.support.CommonServices;
import io.extremum.common.support.ListBasedCommonServices;
import io.extremum.common.support.ListBasedReactiveCommonServices;
import io.extremum.common.support.ModelClasses;
import io.extremum.common.support.ReactiveCommonServices;
import io.extremum.common.support.ScanningModelClasses;
import io.extremum.common.support.UniversalModelFinder;
import io.extremum.common.support.UniversalModelFinderImpl;
import io.extremum.common.uuid.StandardUUIDGenerator;
import io.extremum.common.uuid.UUIDGenerator;
import io.extremum.descriptors.common.AnnotationBasedStumpFacilities;
import io.extremum.descriptors.common.DescriptorsMongoDb;
import io.extremum.descriptors.common.StumpFacilities;
import io.extremum.descriptors.common.dao.DescriptorRepository;
import io.extremum.descriptors.common.properties.DescriptorsProperties;
import io.extremum.descriptors.common.properties.RedisProperties;
import io.extremum.descriptors.common.redisson.ExtremumRedisson;
import io.extremum.descriptors.reactive.config.DescriptorsReactiveMongoConfiguration;
import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDao;
import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDaoFactory;
import io.extremum.descriptors.sync.config.DescriptorsMongoConfiguration;
import io.extremum.descriptors.sync.dao.DescriptorDao;
import io.extremum.descriptors.sync.dao.DescriptorDaoFactory;
import io.extremum.descriptors.sync.dao.impl.JpaDescriptorRepository;
import io.extremum.mapper.jackson.BasicJsonObjectMapper;
import io.extremum.mongo.config.MainMongoConfiguration;
import io.extremum.mongo.config.MainReactiveMongoConfiguration;
import io.extremum.mongo.config.MongoRepositoriesConfiguration;
import io.extremum.mongo.dbfactory.MainMongoDb;
import io.extremum.mongo.reactive.MongoUniversalReactiveModelLoader;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.DescriptorLoader;
import io.extremum.sharedmodels.dto.Constants;
import io.extremum.starter.properties.ModelProperties;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

@Configuration
@Import({MainMongoConfiguration.class, MainReactiveMongoConfiguration.class,
        DescriptorsMongoConfiguration.class, DescriptorsReactiveMongoConfiguration.class,
        MongoRepositoriesConfiguration.class})
@RequiredArgsConstructor
@ComponentScan({"io.extremum.common.dto.converters", "io.extremum.sharedmodels.grpc.converter"})
@EnableConfigurationProperties({RedisProperties.class, DescriptorsProperties.class, ModelProperties.class, IriProperties.class,
        ExceptionProperties.class})
@AutoConfigureBefore(JacksonAutoConfiguration.class)
public class CommonConfiguration {
    private final RedisProperties redisProperties;
    private final DescriptorsProperties descriptorsProperties;
    private final ModelProperties modelProperties;

    @Bean
    @Profile("!secured")
    @ConditionalOnMissingBean
    public AuditorAware<String> auditorProvider() {
        return new NonSecuredAuditorAware();
    }

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return new AuditingDateTimeProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public Config redissonConfig() {
        Config config = new Config();
        config.useSingleServer().setAddress(redisProperties.getUri());
        if (StringUtils.hasLength(redisProperties.getPassword())) {
            config.useSingleServer().setPassword(redisProperties.getPassword());
        }
        return config;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(value = "redis.uri")
    @ConditionalOnMissingBean
    public RedissonClient redissonClient(Config redissonConfig) {
        return new ExtremumRedisson(redissonConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedissonClient.class)
    public RedisConnectionFactory redisConnectionFactory(RedissonClient client) {
        return new RedissonConnectionFactory(client);
    }

    @Bean
    @ConditionalOnProperty(value = "redis.uri")
    @ConditionalOnMissingBean
    public RedissonReactiveClient redissonReactiveClient(Config redissonConfig) {
        return Redisson.createReactive(redissonConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public UUIDGenerator uuidGenerator() {
        return new StandardUUIDGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "extremum", value = "descriptor.storage.type", havingValue = "mongo")
    public DescriptorDao mongoRedisdescriptorDao(RedissonClient redissonClient, DescriptorRepository descriptorRepository,
                                                 @DescriptorsMongoDb MongoOperations descriptorMongoOperations) {
        return DescriptorDaoFactory.createBaseDescriptorDao(redisProperties, descriptorsProperties,
                redissonClient, descriptorRepository, descriptorMongoOperations);
    }

    @Bean
    @ConditionalOnProperty(prefix = "extremum", value = "descriptor.storage.type", havingValue = "memory")
    public DescriptorDao inMemoryDescriptorDao() {
        return DescriptorDaoFactory.createInMemoryDescriptorDao();
    }

    @Bean
    @ConditionalOnProperty(prefix = "extremum", value = "descriptor.storage.type", havingValue = "jpa")
    public DescriptorDao jpaDescriptorDao(RedissonClient redissonClient, DescriptorRepository descriptorRepository, JpaDescriptorRepository jpaDescriptorRepository) {
        return DescriptorDaoFactory.createJpaDescriptorDao(redisProperties, descriptorsProperties,
                redissonClient, descriptorRepository, jpaDescriptorRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "extremum", value = "descriptor.storage.type", havingValue = "mongo")
    public ReactiveDescriptorDao reactiveDescriptorDao(
            RedissonReactiveClient redissonReactiveClient, DescriptorRepository descriptorRepository,
            @DescriptorsMongoDb ReactiveMongoOperations reactiveMongoOperations,
            @MainMongoDb ReactiveMongoDatabaseFactory mongoDatabaseFactory) {
        return ReactiveDescriptorDaoFactory.create(redisProperties, descriptorsProperties,
                redissonReactiveClient, descriptorRepository, reactiveMongoOperations, mongoDatabaseFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "extremum", value = "descriptor.storage.type", havingValue = "jpa")
    public ReactiveDescriptorDao reactiveJpaDescriptorDao(
            DescriptorRepository descriptorRepository) {
        return ReactiveDescriptorDaoFactory.create(descriptorRepository);
    }


    @Bean
    @ConditionalOnMissingBean
    public DescriptorService descriptorService(DescriptorDao descriptorDao, UUIDGenerator uuidGenerator) {
        return new DescriptorServiceImpl(descriptorDao, uuidGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public DescriptorLoader descriptorLoader(DescriptorService descriptorService,
                                             ReactiveDescriptorService reactiveDescriptorService) {
        return new DBDescriptorLoader(descriptorService, reactiveDescriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public StaticDescriptorLoaderAccessorConfigurator staticDescriptorLoaderAccessorConfigurator(
            DescriptorLoader descriptorLoader) {
        return new StaticDescriptorLoaderAccessorConfigurator(descriptorLoader);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveDescriptorService reactiveDescriptorService(ReactiveDescriptorDao reactiveDescriptorDao) {
        return new ReactiveDescriptorServiceImpl(reactiveDescriptorDao);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionDescriptorService collectionDescriptorService(DescriptorService descriptorService,
                                                                   DescriptorDao descriptorDao) {
        return new CollectionDescriptorServiceImpl(descriptorService, descriptorDao);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveCollectionDescriptorExtractor reactiveCollectionDescriptorExtractor(
            List<ReactiveCollectionOverride> extractionOverrides) {
        return new ReactiveCollectionOverridesWithDescriptorExtractorList(extractionOverrides);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveCollectionDescriptorService reactiveCollectionDescriptorService(
            ReactiveDescriptorDao reactiveDescriptorDao, DescriptorService descriptorService,
            ReactiveCollectionDescriptorExtractor collectionDescriptorExtractor) {
        return new ReactiveCollectionDescriptorServiceImpl(reactiveDescriptorDao, descriptorService,
                collectionDescriptorExtractor);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveOwnedModelDescriptorService reactiveOwnedModelDescriptorService(
            ReactiveDescriptorDao reactiveDescriptorDao, DescriptorService descriptorService) {
        return new ReactiveOwnedModelDescriptorServiceImpl(reactiveDescriptorDao, descriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public MapperDependencies mapperDependencies(DescriptorFactory descriptorFactory) {
        return new MapperDependenciesImpl(descriptorFactory);
    }

    @Bean
    @Primary
    public ObjectMapper jacksonObjectMapper(MapperDependencies mapperDependencies,
                                            List<SystemMapperModulesSupplier> systemModulesSuppliers, Locale locale) {
        SystemJsonObjectMapper objectMapper = new SystemJsonObjectMapper(mapperDependencies, locale);
        systemModulesSuppliers.forEach(supplier -> objectMapper.registerModules(supplier.makeModules(objectMapper)));
        return objectMapper;
    }

    @Bean
    @ConditionalOnMissingBean
    public Locale locale(@Nullable @Value("${extremum.application.locale:#{null}}") String locale) {
        if (locale == null || locale.isEmpty()) {
            return Locale.forLanguageTag(Constants.DEFAULT_LOCALE);
        }

        return Locale.forLanguageTag(locale);
    }

    @Bean
    @Qualifier("redis")
    public ObjectMapper redisObjectMapper(List<RedisMapperModulesSupplier> redisModuleSuppliers) {
        BasicJsonObjectMapper objectMapper = new BasicJsonObjectMapper();
        redisModuleSuppliers.forEach(supplier -> objectMapper.registerModules(supplier.makeModules(objectMapper)));
        return objectMapper;
    }

    @Bean
    @ConditionalOnMissingBean
    public DescriptorFactory descriptorFactory() {
        return new DescriptorFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public DescriptorSaver descriptorSaver(DescriptorService descriptorService) {
        return new DescriptorSaver(descriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveDescriptorSaver reactiveDescriptorSaver(DescriptorService descriptorService,
                                                           ReactiveDescriptorService reactiveDescriptorService,
                                                           ReactiveCollectionDescriptorService reactiveCollectionDescriptorService,
                                                           ReactiveOwnedModelDescriptorService reactiveOwnedModelDescriptorService) {
        return new ReactiveDescriptorSaver(descriptorService, reactiveDescriptorService,
                reactiveCollectionDescriptorService, reactiveOwnedModelDescriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public CommonServices commonServices(List<CommonService<? extends Model>> services) {
        return new ListBasedCommonServices(services);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveCommonServices reactiveCommonServices(List<ReactiveCommonService<? extends Model>> services) {
        return new ListBasedReactiveCommonServices(services);
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelClasses modelClasses() {
        return new ScanningModelClasses(modelProperties.getPackageNames());
    }

    @Bean
    @ConditionalOnMissingBean
    public UniversalModelFinder universalModelFinder(ModelClasses modelClasses, CommonServices commonServices) {
        return new UniversalModelFinderImpl(modelClasses, commonServices);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "mongo", value = "uri")
    public MongoUniversalReactiveModelLoader mongoUniversalReactiveModelLoader(
            ReactiveMongoOperations reactiveMongoOperations) {
        return new MongoUniversalReactiveModelLoader(reactiveMongoOperations);
    }

    @Bean(destroyMethod = "dispose")
    public Scheduler reactifierScheduler() {
        // per https://projectreactor.io/docs/core/release/reference/#faq.wrap-blocking
        return Schedulers.elastic();
    }

    @Bean
    @ConditionalOnMissingBean
    public Reactifier reactifier(@Qualifier("reactifierScheduler") Scheduler reactifierScheduler) {
        return new IsolatedSchedulerReactifier(reactifierScheduler);
    }

    @Bean
    public StringToDescriptorConverter stringToDescriptorConverter(DescriptorFactory descriptorFactory) {
        return new StringToDescriptorConverter(descriptorFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public OwnedModelDescriptorSaverAspect ownedModelDescriptorSaverAspect(
            OwnedModelLifecycleSupport ownedModelLifecycleSupport,
            ReactiveOwnedModelDescriptorService reactiveOwnedModelDescriptorService) {
        return new OwnedModelDescriptorSaverAspect(ownedModelLifecycleSupport, reactiveOwnedModelDescriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public BasicModelComposingIriAspect reactiveModelHandlingAspect(IriFacilities iriFacilities) {
        return new BasicModelComposingIriAspect(iriFacilities);
    }

    @Bean
    @ConditionalOnMissingBean
    public IriFacilities defaultIriFacilities(IriProperties iriProperties) {
        return new DefaultIriFacilities(iriProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public OwnedModelLifecycleSupport ownedModelLifecycleSupport() {
        return new OwnedModelLifecycleSupport();
    }

    @Bean
    public ResponseStatusCodeResolver responseStatusCodeResolver() {
        return new HeaderBasedResponseStatusCodeResolver();
    }

    @Bean
    public ExtremumExceptionHandlers extremumExceptionHandlers() {
        return new DefaultExtremumExceptionHandlers();
    }

    @Bean
    @ConditionalOnProperty(value = "exceptions.disable-extremum-handlers", matchIfMissing = true, havingValue = "false")
    public ExtremumExceptionResolver extremumExceptionResolver(List<ExtremumExceptionHandlers> extremumExceptionHandlers) {
        return new AnnotationBasedExtremumExceptionResolver(extremumExceptionHandlers);
    }

    @Bean
    @ConditionalOnProperty(value = "exceptions.disable-extremum-handlers", havingValue = "true")
    public ExtremumExceptionResolver disabledExtremumExceptionResolver() {
        return new DisabledExtremumExceptionResolver();
    }

    @Bean
    public DefaultExceptionHandler defaultExceptionHandler(ExtremumExceptionResolver extremumExceptionHandler) {
        return new DefaultExceptionHandler(extremumExceptionHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public DescriptorIdResolver descriptorIdResolver(DescriptorService descriptorService) {
        return new DescriptorIdResolver(descriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveDescriptorIdResolver reactiveDescriptorIdResolver(ReactiveDescriptorService descriptorService) {
        return new ReactiveDescriptorIdResolver(descriptorService);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public WebFluxConfigurer webFluxConfigurer(ObjectMapper mapper, ExtremumExceptionResolver extremumExceptionResolver) {
        return new WebFluxConfigurer() {
            @Override
            public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
                Jackson2JsonEncoder encoder = new Jackson2JsonEncoder(mapper);
                configurer.customCodecs().register(
                        new ServerSentEventWithErrorMessagesHttpMessageWriter(extremumExceptionResolver, encoder));
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public StumpFacilities stumpFacilities(){
        return new AnnotationBasedStumpFacilities();
    }
}