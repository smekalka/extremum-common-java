package io.extremum.graphql.schema.factory;

import graphql.annotations.AnnotationsSchemaCreator;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import graphql.collect.ImmutableKit;
import graphql.kickstart.autoconfigure.tools.SchemaDirective;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.kickstart.tools.SchemaParser;
import graphql.kickstart.tools.SchemaParserBuilder;
import graphql.kickstart.tools.SchemaParserDictionary;
import graphql.kickstart.tools.SchemaParserOptions;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaPrinter;
import io.extremum.everything.services.models.ModelSettingsProvider;
import io.extremum.graphql.model.relay.DefaultRelay;
import io.extremum.graphql.schema.GraphQlSchemaWrapper;
import io.extremum.graphql.schema.builder.EnhancedGraphQLAnnotations;
import io.extremum.graphql.type.function.ScalarTypeFunction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@AllArgsConstructor
@Slf4j
public class GraphQLSchemaFactory {

    private final List<GraphQLResolver<?>> resolvers;
    private final SchemaParserOptions.Builder optionsBuilder;
    private final SchemaParserDictionary dictionary;
    private final GraphQLScalarType[] scalars;
    private final List<SchemaDirective> directives;
    private final List<SchemaDirectiveWiring> directiveWirings;
    private final List<ScalarTypeFunction> typeFunctions;
    private final String queryClass;
    private final String mutationClass;
    private final String subscriptionClass;
    private final ApplicationContext context;
    private final ModelSettingsProvider modelSettingsProvider;

    public GraphQLSchema getSchema() throws ClassNotFoundException {
        SchemaParser schemaParser = schemaParser(
                resolvers, optionsBuilder, dictionary, scalars, directives, directiveWirings, typeFunctions, queryClass, mutationClass, subscriptionClass, context, modelSettingsProvider
        );
        GraphQLSchema schema = schemaParser.makeExecutableSchema();
        GraphQlSchemaWrapper wrapped = new GraphQlSchemaWrapper(
                schema,
                schema.getCodeRegistry(),
                ImmutableKit.emptyMap(),
                ImmutableKit.emptyMap());
        wrapped.setGraphQLSchema(schema);

        return wrapped;
    }

    public SchemaParser schemaParser(
            List<GraphQLResolver<?>> resolvers,
            SchemaParserOptions.Builder optionsBuilder,
            SchemaParserDictionary dictionary,
            GraphQLScalarType[] scalars,
            List<SchemaDirective> directives,
            List<SchemaDirectiveWiring> directiveWirings,
            List<ScalarTypeFunction> typeFunctions,
            String queryClass,
            String mutationClass,
            String subscriptionClass,
            ApplicationContext context,
            ModelSettingsProvider modelSettingsProvider
    ) throws ClassNotFoundException {
        SchemaParserBuilder schemaParserBuilder = new SchemaParserBuilder();
        EnhancedGraphQLAnnotations enhancedGraphQLAnnotations = new EnhancedGraphQLAnnotations(modelSettingsProvider);

        context.getBeansWithAnnotation(GraphQLTypeExtension.class).forEach(
                (s, o) -> enhancedGraphQLAnnotations.registerTypeExtension(ClassUtils.getUserClass(o.getClass()))
        );
        AnnotationsSchemaCreator.Builder schemaBuilder =
                AnnotationsSchemaCreator
                        .newAnnotationsSchema()
                        .setAnnotationsProcessor(enhancedGraphQLAnnotations)
                        .setRelay(new DefaultRelay())
                        .query(Class.forName(queryClass))
                        .mutation(Class.forName(mutationClass))
                        .subscription(Class.forName(subscriptionClass));

        SchemaPrinter.Options options = SchemaPrinter.Options.defaultOptions();
        options.includeSchemaDefinition(false);
        for (ScalarTypeFunction typeFunction : typeFunctions) {
            schemaBuilder.typeFunction(typeFunction);
        }
        GraphQLSchema schema = schemaBuilder.build();
        log.info("GraphQl schema generated");
        log.info(new SchemaPrinter().print(schema));

        if (nonNull(dictionary)) {
            schemaParserBuilder.dictionary(dictionary.getDictionary());
        }
        List<String> schemaStrings = new ArrayList<>();
        schemaStrings.add(new SchemaPrinter().print(schema));

        schemaStrings.forEach(schemaParserBuilder::schemaString);

        if (scalars != null) {
            schemaParserBuilder.scalars(scalars);
        }

        schemaParserBuilder.options(optionsBuilder.build());
        schemaParserBuilder.scalars(typeFunctions.stream().map(ScalarTypeFunction::getType).collect(Collectors.toList()));

        if (directives != null) {
            directives.forEach(it -> schemaParserBuilder.directive(it.getName(), it.getDirective()));
        }

        if (directiveWirings != null) {
            directiveWirings.forEach(schemaParserBuilder::directiveWiring);
        }

        return schemaParserBuilder.resolvers(resolvers).build();
    }
}
