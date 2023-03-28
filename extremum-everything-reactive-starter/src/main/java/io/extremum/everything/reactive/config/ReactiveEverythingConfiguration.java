package io.extremum.everything.reactive.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.authentication.api.ReactiveSecurityProvider;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorExtractor;
import io.extremum.common.descriptor.resolve.*;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.limit.ReactiveResponseLimiterAspect;
import io.extremum.common.limit.ResponseLimiter;
import io.extremum.common.support.CommonServices;
import io.extremum.common.support.ModelClasses;
import io.extremum.common.support.ReactiveCommonServices;
import io.extremum.everything.config.EverythingCoreConfiguration;
import io.extremum.everything.controllers.EverythingEverythingRestController;
import io.extremum.everything.destroyer.EmptyFieldDestroyer;
import io.extremum.everything.reactive.controller.ReactiveEverythingEverythingRestController;
import io.extremum.everything.services.ReactiveRemovalService;
import io.extremum.everything.services.ReactiveSaverService;
import io.extremum.everything.services.RequestDtoValidator;
import io.extremum.everything.services.defaultservices.*;
import io.extremum.everything.services.management.*;
import io.extremum.everything.support.DefaultModelDescriptors;
import io.extremum.everything.support.DefaultReactiveModelDescriptors;
import io.extremum.everything.support.ModelDescriptors;
import io.extremum.everything.support.ReactiveModelDescriptors;
import io.extremum.security.*;
import io.extremum.security.provider.SecurityProviderReactivePrincipalSource;
import io.extremum.security.provider.SecurityProviderReactiveRoleChecker;
import io.extremum.security.rules.provider.SecurityRuleProvider;
import io.extremum.security.rules.service.SpecFacilities;
import io.extremum.security.services.ReactiveDataAccessChecker;
import io.extremum.starter.CommonConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Locale;

@Configuration
@AutoConfigureAfter(CommonConfiguration.class)
@AutoConfigureBefore({WebMvcAutoConfiguration.class, WebFluxAutoConfiguration.class})
@Import(EverythingCoreConfiguration.class)
public class ReactiveEverythingConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ReactiveGetDemultiplexer reactiveGetDemultiplexer(
            ReactiveEverythingManagementService everythingManagementService,
            EverythingCollectionManagementService everythingCollectionManagementService,
            ReactiveDescriptorService reactiveDescriptorService,
            ReactiveCollectionDescriptorExtractor collectionDescriptorExtractor,
            EverythingOwnedModelManagementService everythingOwnedModelManagementService) {
        return new ReactiveGetDemultiplexerOnDescriptor(everythingManagementService,
                everythingCollectionManagementService, reactiveDescriptorService,
                collectionDescriptorExtractor, everythingOwnedModelManagementService);
    }

    @Bean
    @ConditionalOnMissingBean(EverythingEverythingRestController.class)
    public ReactiveEverythingEverythingRestController everythingEverythingRestController(
            ReactiveEverythingManagementService everythingManagementService,
            EverythingCollectionManagementService everythingCollectionManagementService,
            ReactiveGetDemultiplexer multiplexer, ReactiveDescriptorService reactiveDescriptorService) {
        return new ReactiveEverythingEverythingRestController(everythingManagementService,
                everythingCollectionManagementService, multiplexer, reactiveDescriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    // TODO: move to core?
    public ModelDescriptors modelDescriptors(ModelClasses modelClasses, DescriptorService descriptorService) {
        return new DefaultModelDescriptors(modelClasses, descriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    @Profile("!secured")
    public DefaultGetter defaultGetter(CommonServices commonServices, ModelDescriptors modelDescriptors, ModelClasses modelClasses) {
        return new DefaultGetterViaCommonServices(commonServices, modelDescriptors, modelClasses);
    }

    @Bean
    @ConditionalOnMissingBean
    @Profile("secured")
    public DefaultGetter securityRulesBasedGetter(CommonServices commonServices, ModelDescriptors modelDescriptors, ModelClasses modelClasses, SecurityRuleProvider securityRuleProvider, Locale locale) {
        return new SecurityRulesGetterViaCommonServices(commonServices, modelDescriptors, modelClasses, securityRuleProvider, new SpecFacilities(locale));
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultReactiveGetter defaultReactiveGetter(
            ReactiveCommonServices commonServices, ReactiveModelDescriptors modelDescriptors) {
        return new DefaultReactiveGetterViaReactiveCommonServices(commonServices, modelDescriptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveModelDescriptors reactiveModelDescriptors(ModelClasses modelClasses,
                                                             ReactiveDescriptorService descriptorService) {
        return new DefaultReactiveModelDescriptors(modelClasses, descriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultReactiveSaver defaultReactiveSaver(ReactiveCommonServices commonServices) {
        return new DefaultReactiveSaverImpl(commonServices);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultReactiveRemover defaultReactiveRemover(ReactiveCommonServices commonServices,
                                                         ReactiveModelDescriptors modelDescriptors) {
        return new DefaultReactiveRemoverImpl(commonServices, modelDescriptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveModelSaver reactiveModelSaver(List<ReactiveSaverService<?>> saverServices,
                                                 DefaultReactiveSaver defaultSaver) {
        return new ReactiveModelSaver(saverServices, defaultSaver);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactivePatcher reactivePatcher(
            DtoConversionService dtoConversionService,
            ObjectMapper objectMapper,
            EmptyFieldDestroyer emptyFieldDestroyer,
            RequestDtoValidator requestDtoValidator,
            PatcherHooksCollection hooksCollection
    ) {
        return new ReactivePatcherImpl(dtoConversionService, objectMapper,
                emptyFieldDestroyer, requestDtoValidator, hooksCollection);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactivePatchFlow reactivePatchFlow(
            ModelRetriever modelRetriever,
            ReactivePatcher patcher,
            ReactiveModelSaver modelSaver,
            ReactiveDataSecurity dataSecurity,
            PatcherHooksCollection hooksCollection
    ) {
        return new ReactivePatchFlowImpl(modelRetriever, patcher, modelSaver,
                dataSecurity, hooksCollection);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveSecurityProvider reactiveSecurityProvider() {
        return new NullReactiveSecurityProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveRoleChecker reactiveRoleChecker(ReactiveSecurityProvider securityProvider) {
        return new SecurityProviderReactiveRoleChecker(securityProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactivePrincipalSource reactivePrincipalSource(ReactiveSecurityProvider securityProvider) {
        return new SecurityProviderReactivePrincipalSource(securityProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveRoleSecurity reactiveRoleSecurity(ReactiveRoleChecker roleChecker, ModelClasses modelClasses) {
        return new ModelAnnotationReactiveRoleSecurity(roleChecker, modelClasses);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveDataSecurity reactiveDataSecurity(List<ReactiveDataAccessChecker<?>> checkers,
                                                     ReactiveRoleChecker roleChecker,
                                                     ReactivePrincipalSource principalSource) {
        return new AccessCheckersReactiveDataSecurity(checkers, roleChecker, principalSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveEverythingManagementService reactiveEverythingManagementService(
            ModelRetriever modelRetriever,
            ReactivePatchFlow patchFlow,
            List<ReactiveRemovalService> removalServices,
            DefaultReactiveRemover defaultRemover,
            DtoConversionService dtoConversionService,
            ReactiveRoleSecurity roleSecurity,
            ReactiveDataSecurity dataSecurity,
            ModelNames modelNames) {
        ReactiveEverythingManagementService service = new DefaultReactiveEverythingManagementService(
                modelRetriever,
                patchFlow, removalServices,
                defaultRemover,
                dtoConversionService, dataSecurity, modelNames);
        return new RoleSecurityReactiveEverythingManagementService(service, roleSecurity);
    }

    @Bean
    @ConditionalOnMissingBean
    public EverythingOwnedModelManagementService defaultEverythingOwnedModelManagementService(ModelRetriever modelRetriever, ReactiveDataSecurity reactiveDataSecurity) {
        return new DefaultEverythingOwnedModelManagementService(modelRetriever, reactiveDataSecurity);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveResponseLimiterAspect reactiveResponseLimiterAspect(ResponseLimiter limiter) {
        return new ReactiveResponseLimiterAspect(limiter);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveDescriptorResolver reactiveDescriptorResolver(ReactiveDescriptorService reactiveDescriptorService) {
        return new BatchReactiveDescriptorResolver(reactiveDescriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseDtoDescriptorResolver responseDtoDescriptorResolver(
            ReactiveDescriptorResolver reactiveDescriptorResolver) {
        return new IntrospectingResponseDtoDescriptorResolver(reactiveDescriptorResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveDescriptorResolvingAspect reactiveDescriptorResolvingAspect(
            ResponseDtoDescriptorResolver responseDtoDescriptorResolver) {
        return new ReactiveDescriptorResolvingAspect(responseDtoDescriptorResolver);
    }
}
