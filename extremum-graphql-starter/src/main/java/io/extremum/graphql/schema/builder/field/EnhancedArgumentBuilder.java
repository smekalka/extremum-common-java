package io.extremum.graphql.schema.builder.field;

import graphql.annotations.annotationTypes.GraphQLDefaultValue;
import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;
import graphql.annotations.processor.retrievers.fieldBuilders.DirectivesBuilder;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import io.extremum.graphql.annotations.GraphQLIgnore;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static graphql.annotations.processor.util.NamingKit.toGraphqlName;
import static graphql.annotations.processor.util.ReflectionKit.newInstance;
import static graphql.schema.GraphQLArgument.newArgument;

public class EnhancedArgumentBuilder implements Builder<List<GraphQLArgument>> {
    private final Method method;
    private final TypeFunction typeFunction;
    private final GraphQLFieldDefinition.Builder builder;
    private final ProcessingElementsContainer container;
    private final GraphQLOutputType outputType;

    public EnhancedArgumentBuilder(Method method, TypeFunction typeFunction, GraphQLFieldDefinition.Builder builder, ProcessingElementsContainer container, GraphQLOutputType outputType) {
        this.method = method;
        this.typeFunction = typeFunction;
        this.builder = builder;
        this.container = container;
        this.outputType = outputType;
    }

    @Override
    public List<GraphQLArgument> build() {
        TypeFunction finalTypeFunction = typeFunction;
        List<GraphQLArgument> args = Arrays.stream(method.getParameters()).
                filter(p -> !DataFetchingEnvironment.class.isAssignableFrom(p.getType())).
                filter(p -> p.getAnnotation(GraphQLIgnore.class) == null)
                .map(parameter -> {
                    Class<?> t = parameter.getType();
                    graphql.schema.GraphQLInputType graphQLType = (GraphQLInputType) finalTypeFunction.buildType(true, t, parameter.getAnnotatedType(), container);
                    return getArgument(parameter, graphQLType);
                }).collect(Collectors.toList());

        return args;
    }

    private GraphQLArgument getArgument(Parameter parameter, graphql.schema.GraphQLInputType inputType) throws
            GraphQLAnnotationsException {
        GraphQLArgument.Builder argumentBuilder = newArgument().type(inputType);
        GraphQLDescription description = parameter.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            argumentBuilder.description(description.value());
        }
        GraphQLDefaultValue defaultValue = parameter.getAnnotation(GraphQLDefaultValue.class);
        if (defaultValue != null) {
            argumentBuilder.defaultValue(newInstance(defaultValue.value()).get());
        }
        GraphQLName name = parameter.getAnnotation(GraphQLName.class);
        if (name != null) {
            argumentBuilder.name(toGraphqlName(name.value()));
        } else {
            argumentBuilder.name(toGraphqlName(parameter.getName()));
        }
        argumentBuilder.withDirectives(new DirectivesBuilder(parameter, container).build());
        return argumentBuilder.build();
    }

}
