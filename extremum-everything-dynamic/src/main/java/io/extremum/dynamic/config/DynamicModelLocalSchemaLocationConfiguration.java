package io.extremum.dynamic.config;

import io.extremum.dynamic.HttpSchemaServerLauncher;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.impl.URIBasedNetworkntSchemaProvider;
import io.extremum.dynamic.server.handlers.security.SchemaHandlerSecurityManager;
import io.extremum.dynamic.server.handlers.security.impl.DefaultSchemaHandlerSecurityManager;
import io.extremum.dynamic.server.supports.FilesSupportsService;
import io.extremum.dynamic.server.supports.impl.DefaultFilesSupportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "dynamic-models", value = "schema.location", havingValue = "local")
public class DynamicModelLocalSchemaLocationConfiguration {
    private final DynamicModelProperties props;

    @Bean
    @ConditionalOnMissingBean
    public NetworkntSchemaProvider networkntSchemaProvider() {
        URI baseUri = URI.create(String.format("http://localhost:%d", props.getLocalSchemaServer().getPort()))
                .resolve(props.getLocalSchemaServer().getContextPath());
        return new URIBasedNetworkntSchemaProvider(baseUri);
    }

    @Bean
    @Qualifier("localSchemaServerExecutor")
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(5);
    }

    @Bean
    @ConditionalOnMissingBean
    public FilesSupportsService filesSupportsService() {
        return new DefaultFilesSupportsService();
    }

    @Bean
    @ConditionalOnMissingBean
    public SchemaHandlerSecurityManager schemaHandlerSecurityManager() {
        return new DefaultSchemaHandlerSecurityManager(Paths.get(props.getSchema().getPointer().getLocal().getBaseDirectory()));
    }

    @Bean
    public HttpSchemaServerLauncher httpSchemaServer(@Qualifier("localSchemaServerExecutor") ExecutorService executor,
                                                     FilesSupportsService fileSupportsService, SchemaHandlerSecurityManager securityManager) {
        return new HttpSchemaServerLauncher(executor,
                Paths.get(props.getSchema().getPointer().getLocal().getBaseDirectory()),
                props.getLocalSchemaServer().getPort(),
                props.getLocalSchemaServer().getContextPath(),
                fileSupportsService,
                securityManager
        );
    }
}
