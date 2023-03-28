package io.extremum.rdf.triple.config;

import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.everything.services.management.EverythingEverythingManagementService;
import io.extremum.rdf.triple.service.IriResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;

@Configuration
@ComponentScan(basePackages = "io.extremum.rdf.triple")
@Import({TripleMongoConfiguration.class, TripleJpaConfiguration.class})
public class TripleAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IriResolver iriResolver(@Lazy ReactiveDescriptorService reactiveDescriptorService, @Lazy EverythingEverythingManagementService service) {
        IriResolver iriResolver = new IriResolver(reactiveDescriptorService);
        iriResolver.setService(service);

        return iriResolver;
    }
}