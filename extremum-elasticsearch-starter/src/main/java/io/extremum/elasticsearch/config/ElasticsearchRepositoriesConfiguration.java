package io.extremum.elasticsearch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.descriptor.factory.impl.DescriptorIdResolver;
import io.extremum.common.descriptor.factory.impl.ReactiveDescriptorIdResolver;
import io.extremum.elasticsearch.facilities.ElasticsearchDescriptorFacilities;
import io.extremum.elasticsearch.facilities.ElasticsearchDescriptorFacilitiesImpl;
import io.extremum.elasticsearch.facilities.ReactiveElasticsearchDescriptorFacilities;
import io.extremum.elasticsearch.facilities.ReactiveElasticsearchDescriptorFacilitiesImpl;
import io.extremum.elasticsearch.properties.ElasticsearchProperties;
import io.extremum.elasticsearch.reactive.ElasticsearchUniversalReactiveModelLoader;
import io.extremum.elasticsearch.service.lifecycle.ElasticsearchCommonModelLifecycleCallbacks;
import io.extremum.elasticsearch.service.lifecycle.ReactiveElasticsearchCommonModelLifecycleCallbacks;
import io.extremum.elasticsearch.springdata.reactiverepository.EnableExtremumReactiveElasticsearchRepositories;
import io.extremum.elasticsearch.springdata.reactiverepository.ExtremumReactiveElasticsearchRepositoryFactoryBean;
import io.extremum.elasticsearch.springdata.reactiverepository.ExtremumReactiveElasticsearchTemplate;
import io.extremum.elasticsearch.springdata.repository.EnableExtremumElasticsearchRepositories;
import io.extremum.elasticsearch.springdata.repository.ExtremumElasticsearchRepositoryFactoryBean;
import io.extremum.elasticsearch.springdata.repository.ExtremumElasticsearchRestTemplate;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.DefaultReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.config.EnableElasticsearchAuditing;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.http.HttpHeaders;

@Configuration
@ConditionalOnProperty("elasticsearch.repository-packages")
@EnableConfigurationProperties(ElasticsearchProperties.class)
@EnableExtremumElasticsearchRepositories(basePackages = "${elasticsearch.repository-packages}",
        repositoryFactoryBeanClass = ExtremumElasticsearchRepositoryFactoryBean.class)
@EnableExtremumReactiveElasticsearchRepositories(basePackages = "${elasticsearch.repository-packages}",
        repositoryFactoryBeanClass = ExtremumReactiveElasticsearchRepositoryFactoryBean.class)
@EnableElasticsearchAuditing(dateTimeProviderRef = "dateTimeProvider")
@RequiredArgsConstructor
public class ElasticsearchRepositoriesConfiguration {
    private final ElasticsearchProperties elasticsearchProperties;

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchDescriptorFacilities elasticsearchDescriptorFacilities(DescriptorFactory descriptorFactory,
            DescriptorSaver descriptorSaver, DescriptorIdResolver descriptorIdResolver) {
        return new ElasticsearchDescriptorFacilitiesImpl(descriptorFactory, descriptorSaver, descriptorIdResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveElasticsearchDescriptorFacilities reactiveElasticsearchDescriptorFacilities(
            DescriptorFactory descriptorFactory, ReactiveDescriptorSaver descriptorSaver,
            ReactiveDescriptorIdResolver descriptorIdResolver) {
        return new ReactiveElasticsearchDescriptorFacilitiesImpl(descriptorFactory, descriptorSaver, descriptorIdResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public RestHighLevelClient elasticsearchClient() {
        HttpHost[] httpHosts = elasticsearchProperties.getHosts().stream()
                .map(h -> new HttpHost(h.getHost(), h.getPort(), h.getProtocol()))
                .toArray(HttpHost[]::new);
        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);

        if (elasticsearchProperties.hasAuth()) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(elasticsearchProperties.getUsername(), elasticsearchProperties.getPassword()));
            restClientBuilder.setHttpClientConfigCallback(clientBuilder -> clientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider));
        }
        return new RestHighLevelClient(restClientBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchOperations elasticsearchTemplate(RestHighLevelClient elasticsearchClient,
            ElasticsearchConverter elasticsearchConverter) {
        return new ExtremumElasticsearchRestTemplate(elasticsearchClient, elasticsearchConverter
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public SimpleElasticsearchMappingContext elasticsearchMappingContext() {
        return new SimpleElasticsearchMappingContext();
    }

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchConverter elasticsearchConverter(SimpleElasticsearchMappingContext mappingContext) {
        return new MappingElasticsearchConverter(mappingContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveElasticsearchClient reactiveElasticsearchClient() {
        String[] hosts = elasticsearchProperties.getHosts().stream()
                .map(h -> h.getHost() + ":" + h.getPort())
                .toArray(String[]::new);

        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(hosts);
        if (elasticsearchProperties.isUsingSsl()) {
            builder.usingSsl();
        }
        if (elasticsearchProperties.hasAuth()) {
            builder.withBasicAuth(elasticsearchProperties.getUsername(),
                    elasticsearchProperties.getPassword());
        }
        ClientConfiguration clientConfiguration = builder
                .withDefaultHeaders(HttpHeaders.EMPTY)
                .build();

        return DefaultReactiveElasticsearchClient.create(clientConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveElasticsearchOperations reactiveElasticsearchTemplate(
            ReactiveElasticsearchClient reactiveElasticsearchClient,
            ElasticsearchConverter elasticsearchConverter, ObjectMapper objectMapper) {
        return new ExtremumReactiveElasticsearchTemplate(reactiveElasticsearchClient,
                elasticsearchConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchUniversalReactiveModelLoader elasticsearchUniversalReactiveModelLoader(
            ReactiveElasticsearchOperations reactiveElasticsearchOperations) {
        return new ElasticsearchUniversalReactiveModelLoader(reactiveElasticsearchOperations);
    }

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchCommonModelLifecycleCallbacks elasticsearchCommonModelLifecycleCallbacks(
            ElasticsearchDescriptorFacilities descriptorFacilities) {
        return new ElasticsearchCommonModelLifecycleCallbacks(descriptorFacilities);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveElasticsearchCommonModelLifecycleCallbacks reactiveElasticsearchCommonModelLifecycleCallbacks(
            ReactiveElasticsearchDescriptorFacilities descriptorFacilities) {
        return new ReactiveElasticsearchCommonModelLifecycleCallbacks(descriptorFacilities);
    }

}
