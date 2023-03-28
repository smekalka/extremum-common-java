package io.extremum.graphql.config;


import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.extremum.graphql.schema.GraphQlSchemaWrapper;
import io.extremum.graphql.schema.factory.GraphQLSchemaFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;

@Slf4j
public class SchemaReloader {
    private final GraphQlSchemaWrapper graphQLSchema;
    private final GraphQLSchemaFactory graphQLSchemaFactory;

    public SchemaReloader(GraphQlSchemaWrapper graphQLSchema, GraphQLSchemaFactory graphQLSchemaFactory, SubscribableChannel modelSettingsMessageChannel) {
        this.graphQLSchema = graphQLSchema;
        this.graphQLSchemaFactory = graphQLSchemaFactory;
        modelSettingsMessageChannel.subscribe(this::reloadSchema);
    }

    @SneakyThrows
    public void reloadSchema(Message<?> message) {
        log.info("Refreshing schema");
        GraphQLSchema newSchema = graphQLSchemaFactory.getSchema();
        String print = new SchemaPrinter().print(newSchema);
        log.info("Generated new graphql schema");
        System.out.println(print);
        graphQLSchema.setGraphQLSchema(newSchema);
    }
}