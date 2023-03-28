package io.extremum.dynamic.config;

import io.extremum.dynamic.GithubWebhookListenerLauncher;
import io.extremum.dynamic.resources.github.GithubAccessOptions;
import io.extremum.dynamic.resources.github.GithubResourceConfiguration;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.caching.NetworkntCacheManager;
import io.extremum.dynamic.schema.provider.networknt.caching.impl.CachingGithubNetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.caching.impl.MemoryNetworkntCacheManager;
import io.extremum.dynamic.schema.provider.networknt.impl.GithubNetworkntSchemaProvider;
import io.extremum.dynamic.server.impl.GithubWebhookListenerHttpSchemaServer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collection;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "dynamic-models", value = "schema.location", havingValue = "github")
public class DynamicModelGithubSchemaLocationConfiguration {
    private final DynamicModelProperties props;

    @Bean
    @ConditionalOnMissingBean
    public NetworkntCacheManager networkntCacheManager() {
        return new MemoryNetworkntCacheManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public NetworkntSchemaProvider networkntSchemaProvider(NetworkntCacheManager cacheManager) {
        GithubResourceConfiguration githubResConfig = new GithubResourceConfiguration(
                props.getSchema().getPointer().getGithub().getOwner(),
                props.getSchema().getPointer().getGithub().getRepo(),
                props.getSchema().getPointer().getSchemaPath(),
                props.getSchema().getPointer().getGithub().getRef()
        );

        GithubAccessOptions githubAccessOpts = new GithubAccessOptions(
                props.getSchema().getPointer().getGithub().getToken()
        );

        GithubNetworkntSchemaProvider githubNetworkntSchemaProvider = new GithubNetworkntSchemaProvider(JsonSchemaType.V2019_09, githubResConfig, githubAccessOpts);

        return new CachingGithubNetworkntSchemaProvider(cacheManager, githubNetworkntSchemaProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public GithubWebhookListenerHttpSchemaServer githubWebhookListenerHttpSchemaServer(Collection<NetworkntCacheManager> cacheManagers) {
        return new GithubWebhookListenerHttpSchemaServer(
                props.getGithubWebHookListener().getPort(),
                props.getGithubWebHookListener().getServerContext(),
                cacheManagers
        );
    }

    @Bean
    @ConditionalOnBean(GithubWebhookListenerHttpSchemaServer.class)
    public GithubWebhookListenerLauncher githubWebhookListenerLauncher(GithubWebhookListenerHttpSchemaServer server) {
        return new GithubWebhookListenerLauncher(server);
    }
}
