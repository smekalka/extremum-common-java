package io.extremum.everything.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.collection.conversion.CollectionMakeup;
import io.extremum.common.collection.conversion.CollectionMakeupImpl;
import io.extremum.common.collection.conversion.CollectionMakeupModule;
import io.extremum.common.collection.conversion.CollectionUrls;
import io.extremum.common.collection.conversion.CollectionUrlsInRoot;
import io.extremum.common.collection.conversion.OwnedCollectionReferenceCollector;
import io.extremum.common.collection.conversion.ReactiveResponseCollectionsMakeupAspect;
import io.extremum.common.collection.conversion.ResponseCollectionsMakeupAdvice;
import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.limit.ResponseLimiter;
import io.extremum.common.limit.ResponseLimiterImpl;
import io.extremum.common.reactive.Reactifier;
import io.extremum.common.response.status.ResponseStatusCodeResolver;
import io.extremum.common.support.ListBasedUniversalReactiveModelLoaders;
import io.extremum.common.support.ModelClasses;
import io.extremum.common.support.UniversalReactiveModelLoader;
import io.extremum.common.support.UniversalReactiveModelLoaders;
import io.extremum.common.tx.CollectionTransactivity;
import io.extremum.common.tx.CollectionTransactor;
import io.extremum.common.tx.TransactorsCollectionTransactivity;
import io.extremum.common.urls.ApplicationUrls;
import io.extremum.common.urls.ApplicationUrlsImpl;
import io.extremum.everything.aop.ConvertNullDescriptorToModelNotFoundAspect;
import io.extremum.everything.aop.DefaultEverythingEverythingExceptionHandler;
import io.extremum.everything.aop.EverythingEverythingExceptionHandler;
import io.extremum.everything.config.properties.DestroyerProperties;
import io.extremum.everything.controllers.PingController;
import io.extremum.everything.dao.InMemoryUniversalDao;
import io.extremum.everything.dao.JpaUniversalDao;
import io.extremum.everything.dao.MongoUniversalDao;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.destroyer.EmptyFieldDestroyer;
import io.extremum.everything.destroyer.EmptyFieldDestroyerConfig;
import io.extremum.everything.destroyer.PublicEmptyFieldDestroyer;
import io.extremum.everything.services.DefaultRequestDtoValidator;
import io.extremum.everything.services.FreeCollectionFetcher;
import io.extremum.everything.services.FreeCollectionStreamer;
import io.extremum.everything.services.GetterService;
import io.extremum.everything.services.OwnedCollectionFetcher;
import io.extremum.everything.services.OwnedCollectionStreamer;
import io.extremum.everything.services.PatcherHooksService;
import io.extremum.everything.services.ReactiveGetterService;
import io.extremum.everything.services.RequestDtoValidator;
import io.extremum.everything.services.collection.CollectionProviders;
import io.extremum.everything.services.collection.DefaultEverythingCollectionService;
import io.extremum.everything.services.collection.EverythingCollectionService;
import io.extremum.everything.services.collection.FillCollectionTop;
import io.extremum.everything.services.collection.ListBasedCollectionProviders;
import io.extremum.everything.services.defaultservices.DefaultGetter;
import io.extremum.everything.services.defaultservices.DefaultReactiveGetter;
import io.extremum.everything.services.management.DefaultEverythingCollectionManagementService;
import io.extremum.everything.services.management.DefaultModelCollectionNameResolver;
import io.extremum.everything.services.management.EverythingCollectionManagementService;
import io.extremum.everything.services.management.ModelCollectionNameResolver;
import io.extremum.everything.services.management.ModelNames;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.everything.services.management.ModelSaver;
import io.extremum.everything.services.management.PatcherHooksCollection;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.starter.properties.LimitsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.web.method.annotation.RequestHeaderMapMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({DestroyerProperties.class, LimitsProperties.class})
public class EverythingCoreConfiguration {
    private final DestroyerProperties destroyerProperties;
    private final LimitsProperties limitsProperties;

    @Bean
    @ConditionalOnMissingBean
    public EmptyFieldDestroyer emptyFieldDestroyer() {
        EmptyFieldDestroyerConfig config = new EmptyFieldDestroyerConfig();
        config.setAnalyzablePackagePrefixes(destroyerProperties.getAnalyzablePackagePrefix());
        return new PublicEmptyFieldDestroyer(config);
    }

    @Bean
    @ConditionalOnMissingBean(RequestDtoValidator.class)
    public RequestDtoValidator requestDtoValidator() {
        return new DefaultRequestDtoValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public PingController pingController() {
        return new PingController();
    }

    @Bean
    @ConditionalOnMissingBean(EverythingEverythingExceptionHandler.class)
    public EverythingEverythingExceptionHandler everythingEverythingExceptionHandler(ResponseStatusCodeResolver statusCodeResolver) {
        return new DefaultEverythingEverythingExceptionHandler(statusCodeResolver);
    }

    @Bean
    public ConvertNullDescriptorToModelNotFoundAspect convertNullDescriptorToModelNotFoundAspect() {
        return new ConvertNullDescriptorToModelNotFoundAspect();
    }

    @Bean
    @ConditionalOnProperty(prefix = "extremum", value = "everything.storage.type", havingValue = "mongo")
    public UniversalDao mongoUniversalDao(MongoOperations mongoOperations, ReactiveMongoOperations reactiveMongoOperations) {
        return new MongoUniversalDao(mongoOperations, reactiveMongoOperations);
    }

    @Bean
    @ConditionalOnProperty(prefix = "extremum", value = "everything.storage.type", havingValue = "memory")
    public UniversalDao inMemoryUniversalDao() {
        return new InMemoryUniversalDao();
    }


    @Bean
    @ConditionalOnProperty(prefix = "extremum", value = "everything.storage.type", havingValue = "jpa")
    public UniversalDao jpaUniversalDao() {
        return new JpaUniversalDao();
    }

    @Bean
    @ConditionalOnMissingBean
    public UniversalReactiveModelLoaders universalReactiveModelLoader(List<UniversalReactiveModelLoader> loaders, ModelClasses modelClasses) {
        return new ListBasedUniversalReactiveModelLoaders(loaders, modelClasses);
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelRetriever modelRetriever(List<GetterService<?>> getterServices, List<ReactiveGetterService<?>> reactiveGetterServices, DefaultGetter defaultGetter, DefaultReactiveGetter defaultReactiveGetter, ModelNames modelNames) {
        return new ModelRetriever(getterServices, reactiveGetterServices, defaultGetter, defaultReactiveGetter, modelNames);
    }

    @Bean
    @ConditionalOnMissingBean
    public PatcherHooksCollection patcherHooksCollection(List<PatcherHooksService<?, ?>> patcherHooksServices) {
        return new PatcherHooksCollection(patcherHooksServices);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionTransactivity collectionTransactivity(List<CollectionTransactor> transactors) {
        return new TransactorsCollectionTransactivity(transactors);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionProviders collectionProviders(List<OwnedCollectionFetcher> ownedCollectionFetchers, List<OwnedCollectionStreamer> ownedCollectionStreamers, List<FreeCollectionFetcher<? extends Model>> freeCollectionFetchers, List<FreeCollectionStreamer<? extends Model>> freeCollectionStreamers) {
        return new ListBasedCollectionProviders(ownedCollectionFetchers, ownedCollectionStreamers, freeCollectionFetchers, freeCollectionStreamers);
    }

    @Bean
    @ConditionalOnMissingBean
    public EverythingCollectionService everythingCollectionService(ModelRetriever modelRetriever,
                                                                   CollectionProviders collectionProviders,
                                                                   DtoConversionService dtoConversionService,
                                                                   UniversalDao universalDao,
                                                                   Reactifier reactifier,
                                                                   CollectionTransactivity transactivity,
                                                                   ModelSaver modelSaver
                                                                   ) {
        return new DefaultEverythingCollectionService(modelRetriever, collectionProviders, dtoConversionService, universalDao, reactifier, transactivity, modelSaver);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationUrls applicationUrls(@Value("${extremum.application.host}") String appHost) {
        return new ApplicationUrlsImpl(appHost);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionUrls collectionUrls(ApplicationUrls applicationUrls) {
        return new CollectionUrlsInRoot(applicationUrls);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionMakeup collectionMakeup(CollectionDescriptorService collectionDescriptorService, ReactiveCollectionDescriptorService reactiveCollectionDescriptorService, CollectionUrls collectionUrls, List<CollectionMakeupModule> makeupModules) {
        return new CollectionMakeupImpl(collectionDescriptorService, reactiveCollectionDescriptorService, collectionUrls, makeupModules, new OwnedCollectionReferenceCollector());
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseCollectionsMakeupAdvice responseCollectionsMakeupAdvice(CollectionMakeup collectionMakeup) {
        return new ResponseCollectionsMakeupAdvice(collectionMakeup);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveResponseCollectionsMakeupAspect reactiveResponseCollectionsMakeupAspect(CollectionMakeup collectionMakeup) {
        return new ReactiveResponseCollectionsMakeupAspect(collectionMakeup);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseLimiter responseLimiter(ObjectMapper objectMapper) {
        return new ResponseLimiterImpl(limitsProperties.getCollectionTopMaxSizeBytes(), objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public EverythingCollectionManagementService everythingCollectionManagementService(ReactiveCollectionDescriptorService reactiveCollectionDescriptorService, EverythingCollectionService everythingCollectionService) {
        return new DefaultEverythingCollectionManagementService(reactiveCollectionDescriptorService, everythingCollectionService);
    }

    @Bean
    public FillCollectionTop fillCollectionTop(EverythingCollectionService everythingCollectionService) {
        return new FillCollectionTop(everythingCollectionService);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public WebMvcConfigurer coreWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new RequestHeaderMapMethodArgumentResolver());
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelNames modelNames(ModelCollectionNameResolver modelCollectionNameResolver) {
        return new ModelNames(modelCollectionNameResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelCollectionNameResolver modelCollectionNameResolver() {
        return new DefaultModelCollectionNameResolver();
    }
}
