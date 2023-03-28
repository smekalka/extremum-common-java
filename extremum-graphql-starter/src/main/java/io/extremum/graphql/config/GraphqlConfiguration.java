package io.extremum.graphql.config;

import graphql.collect.ImmutableKit;
import graphql.kickstart.autoconfigure.tools.SchemaDirective;
import graphql.kickstart.execution.error.GraphQLErrorHandler;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.kickstart.tools.SchemaParser;
import graphql.kickstart.tools.SchemaParserDictionary;
import graphql.kickstart.tools.SchemaParserOptions;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaDirectiveWiring;
import io.extremum.common.support.ModelClasses;
import io.extremum.dao.DataAccessExceptionTranslators;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.everything.services.management.ModelSaver;
import io.extremum.everything.services.models.ModelSettingsProvider;
import io.extremum.graphql.dao.AdvancedCommonDao;
import io.extremum.graphql.dao.jpa.AdvancedQueryBuilder;
import io.extremum.graphql.dao.jpa.DefaultQueryBuilder;
import io.extremum.graphql.dao.jpa.JpaAdvancedCommonDao;
import io.extremum.graphql.exception.ExtremumGraphQLErrorHandler;
import io.extremum.graphql.schema.GraphQlSchemaWrapper;
import io.extremum.graphql.schema.factory.AnnotationSchemaCreatorBuilderFactory;
import io.extremum.graphql.schema.factory.GraphQLSchemaFactory;
import io.extremum.graphql.type.function.ScalarTypeFunction;
import io.extremum.security.model.jwt.OidcJwtTokenConverter;
import io.extremum.security.rules.service.SpecFacilities;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Locale;

@Configuration
@ComponentScan({"io.extremum.graphql.type.function"})
@Slf4j
public class GraphqlConfiguration {

    @Bean
    public GraphqlTransactionInstrumentation graphqlTransactionInstrumentation(PlatformTransactionManager transactionManager, DataAccessExceptionTranslators dataAccessExceptionTranslators) {
        return new GraphqlTransactionInstrumentation(transactionManager, dataAccessExceptionTranslators);
    }

    @Bean
    public GraphqlWebSocketSecurityContextInstrumentation graphqlWebSocketSecurityContextInstrumentation(PlatformTransactionManager transactionManager) {
        return new GraphqlWebSocketSecurityContextInstrumentation();
    }

    @Bean
    public AuthenticationConnectionListener authenticationConnectionListener(JwtDecoder decoder, OidcJwtTokenConverter converter) {
        return new AuthenticationConnectionListener(decoder, converter);
    }

    @Bean
    @ConditionalOnMissingBean
    public AdvancedCommonDao commonGraphQLDao(ModelRetriever modelRetriever, ModelSaver modelSaver, ModelClasses modelClasses, AdvancedQueryBuilder advancedQueryBuilder, EntityManager entityManager, Locale locale) {
        return new JpaAdvancedCommonDao(modelRetriever, modelSaver, modelClasses, advancedQueryBuilder, entityManager, new SpecFacilities(locale));
    }

    @Bean
    @ConditionalOnMissingBean
    public AdvancedQueryBuilder advancedQueryBuilder(EntityManager entityManager) {
        return new DefaultQueryBuilder(entityManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public GraphQLErrorHandler graphQLErrorHandler() {
        return new ExtremumGraphQLErrorHandler();
    }


    @Bean
    public AnnotationSchemaCreatorBuilderFactory annotationSchemaCreatorBuilderFactory(
            ApplicationContext applicationContext,
            @Value("${extremum.graphql.query-class}") String queryClass,
            @Value("${extremum.graphql.mutation-class}") String mutationClass,
            @Value("${extremum.graphql.subscription-class}") String subscriptionClass,
            @Autowired(required = false) ModelSettingsProvider modelSettingsProvider,
            @Autowired(required = false) List<ScalarTypeFunction> typeFunctions
    ) {
        return new AnnotationSchemaCreatorBuilderFactory(
                applicationContext,
                queryClass,
                mutationClass,
                subscriptionClass,
                modelSettingsProvider,
                typeFunctions
        );
    }

    @SneakyThrows
    @Bean
    @ConditionalOnBean({GraphQLResolver.class})
    public GraphQLSchema graphQLSchema(SchemaParser schemaParser) {
        GraphQLSchema schema = schemaParser.makeExecutableSchema();
        GraphQlSchemaWrapper wrapped = new GraphQlSchemaWrapper(
                schema,
                schema.getCodeRegistry(),
                ImmutableKit.emptyMap(),
                ImmutableKit.emptyMap());
        wrapped.setGraphQLSchema(schema);

        return wrapped;
    }

    @Bean
    public GraphQLSchemaFactory graphQLSchemaFactory(
            List<GraphQLResolver<?>> resolvers,
            SchemaParserOptions.Builder optionsBuilder,
            @Autowired(required = false) SchemaParserDictionary dictionary,
            @Autowired(required = false) GraphQLScalarType[] scalars,
            @Autowired(required = false) List<SchemaDirective> directives,
            @Autowired(required = false) List<SchemaDirectiveWiring> directiveWirings,
            @Autowired(required = false) List<ScalarTypeFunction> typeFunctions,
            @Value("${extremum.graphql.query-class}") String queryClass,
            @Value("${extremum.graphql.mutation-class}") String mutationClass,
            @Value("${extremum.graphql.subscription-class}") String subscriptionClass,
            ApplicationContext context,
            ModelSettingsProvider modelSettingsProvider
    ) {
        return new GraphQLSchemaFactory(resolvers, optionsBuilder, dictionary, scalars, directives, directiveWirings, typeFunctions, queryClass, mutationClass, subscriptionClass, context, modelSettingsProvider);
    }

    @Bean
    public SchemaReloader schemaReloader(
            GraphQlSchemaWrapper graphQlSchemaWrapper,
            GraphQLSchemaFactory graphQLSchemaFactory,
            @Qualifier("modelSettingsMessageChannel") SubscribableChannel modelSettingMessageChannel
    ) {
        return new SchemaReloader(graphQlSchemaWrapper, graphQLSchemaFactory, modelSettingMessageChannel);
    }

    @Bean("modelSettingsMessageChannel")
    public SubscribableChannel modelSettingsMessageChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    @SneakyThrows
    public SchemaParser schemaParser(
            List<GraphQLResolver<?>> resolvers,
            SchemaParserOptions.Builder optionsBuilder,
            @Autowired(required = false) SchemaParserDictionary dictionary,
            @Autowired(required = false) GraphQLScalarType[] scalars,
            @Autowired(required = false) List<SchemaDirective> directives,
            @Autowired(required = false) List<SchemaDirectiveWiring> directiveWirings,
            @Autowired(required = false) List<ScalarTypeFunction> typeFunctions,
            @Value("${extremum.graphql.query-class}") String queryClass,
            @Value("${extremum.graphql.mutation-class}") String mutationClass,
            @Value("${extremum.graphql.subscription-class}") String subscriptionClass,
            ApplicationContext context,
            ModelSettingsProvider modelSettingsProvider,
            GraphQLSchemaFactory graphQLSchemaFactory
    ) {
        return graphQLSchemaFactory.schemaParser(
                resolvers, optionsBuilder, dictionary, scalars, directives, directiveWirings, typeFunctions, queryClass, mutationClass, subscriptionClass, context, modelSettingsProvider);
    }
}
