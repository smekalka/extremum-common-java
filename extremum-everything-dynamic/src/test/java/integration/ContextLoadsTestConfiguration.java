package integration;

import configurations.FileSystemSchemaProviderConfiguration;
import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.everything.reactive.config.ReactiveEverythingConfiguration;
import io.extremum.starter.CommonConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({
        CommonConfiguration.class,
        ReactiveEverythingConfiguration.class,
        FileSystemSchemaProviderConfiguration.class,
        DynamicModuleAutoConfiguration.class
})
@Configuration
public class ContextLoadsTestConfiguration {
}
