package io.extremum.graphql.schema.factory;

import graphql.annotations.AnnotationsSchemaCreator;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import io.extremum.everything.services.models.ModelSettingsProvider;
import io.extremum.graphql.model.relay.DefaultRelay;
import io.extremum.graphql.schema.builder.EnhancedGraphQLAnnotations;
import io.extremum.graphql.type.function.ScalarTypeFunction;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;


@AllArgsConstructor
public class AnnotationSchemaCreatorBuilderFactory {
    private final ApplicationContext context;
    private final String queryClass;
    private final String mutationClass;
    private final String subscriptionClass;

    private final  ModelSettingsProvider modelSettingsProvider;
    private final  List<ScalarTypeFunction> typeFunctions;

    public AnnotationsSchemaCreator.Builder getBuilder() throws ClassNotFoundException {
        EnhancedGraphQLAnnotations enhancedGraphQLAnnotations = new EnhancedGraphQLAnnotations(modelSettingsProvider);

        context.getBeansWithAnnotation(GraphQLTypeExtension.class)
                .forEach(
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

        for (ScalarTypeFunction typeFunction : typeFunctions) {
            schemaBuilder.typeFunction(typeFunction);
        }
        return schemaBuilder;

    }
}
