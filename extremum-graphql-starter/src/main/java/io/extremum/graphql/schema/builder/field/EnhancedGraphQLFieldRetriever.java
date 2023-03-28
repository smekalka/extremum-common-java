package io.extremum.graphql.schema.builder.field;


import graphql.annotations.annotationTypes.GraphQLRelayMutation;
import graphql.annotations.connection.GraphQLConnection;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.retrievers.GraphQLFieldRetriever;
import graphql.annotations.processor.retrievers.fieldBuilders.DeprecateBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.DescriptionBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.DirectivesBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.field.FieldDataFetcherBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.field.FieldNameBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.method.MethodDataFetcherBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.method.MethodNameBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.method.MethodTypeBuilder;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.annotations.processor.util.CodeRegistryUtil;
import graphql.annotations.processor.util.DataFetcherConstructor;
import graphql.relay.Relay;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.StaticDataFetcher;
import io.extremum.graphql.annotations.GraphQLSubscription;
import io.extremum.graphql.schema.ConnectionUtil;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static graphql.annotations.processor.util.GraphQLTypeNameResolver.getName;
import static graphql.annotations.processor.util.ReflectionKit.newInstance;
import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;

public class EnhancedGraphQLFieldRetriever extends GraphQLFieldRetriever {

    private DataFetcherConstructor dataFetcherConstructor;

    private boolean alwaysPrettify = false;

    public EnhancedGraphQLFieldRetriever(DataFetcherConstructor dataFetcherConstructor) {
        this.dataFetcherConstructor = dataFetcherConstructor;
    }

    public EnhancedGraphQLFieldRetriever() {
        this(new DataFetcherConstructor());
    }

    public GraphQLFieldDefinition getField(String parentName, Method method, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        GraphQLFieldDefinition.Builder builder = newFieldDefinition();
        TypeFunction typeFunction = getTypeFunction(method, container);
        String fieldName = new MethodNameBuilder(method).alwaysPrettify(alwaysPrettify).build();
        builder.name(fieldName);
        GraphQLOutputType outputType = (GraphQLOutputType) new MethodTypeBuilder(method, typeFunction, container, false).build();

        boolean isConnection = ConnectionUtil.isConnection(method, outputType);
        if (isConnection) {
            outputType = getGraphQLConnection(method, outputType, ConnectionUtil.getRelay(method, container), container.getTypeRegistry());
            builder.arguments(ConnectionUtil.getRelay(method, container).getConnectionFieldArguments());
        }
        boolean isSubscription = isSubscription(method, outputType);
        if (isSubscription) {
            outputType = (GraphQLOutputType) new SubscriptionMethodTypeBuilder(method, typeFunction, container, false).build();
        }
        builder.type(outputType);
        DirectivesBuilder directivesBuilder = new DirectivesBuilder(method, container);
        builder.withDirectives(directivesBuilder.build());
        List<GraphQLArgument> args = new EnhancedArgumentBuilder(method, typeFunction, builder, container, outputType).build();
        GraphQLFieldDefinition relayFieldDefinition = handleRelayArguments(method, container, builder, outputType, args);
        builder.description(new DescriptionBuilder(method).build())
                .deprecate(new DeprecateBuilder(method).build())
                .build();

        DataFetcher dataFetcher = new MethodDataFetcherBuilder(method, outputType, typeFunction, container, relayFieldDefinition, args, dataFetcherConstructor, isConnection).build();
        container.getCodeRegistryBuilder().dataFetcher(coordinates(parentName, fieldName), dataFetcher);
        return builder.build();
    }

    public static boolean isSubscription(AccessibleObject obj, GraphQLType type) {
        return obj.isAnnotationPresent(GraphQLSubscription.class);
    }

    public GraphQLFieldDefinition getField(String parentName, Field field, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        GraphQLFieldDefinition.Builder builder = newFieldDefinition();
        String fieldName = new FieldNameBuilder(field).alwaysPrettify(alwaysPrettify).build();
        builder.name(fieldName);
        TypeFunction typeFunction = getTypeFunction(field, container);

        GraphQLType outputType = typeFunction.buildType(field.getType(), field.getAnnotatedType(), container);
        boolean isConnection = ConnectionUtil.isConnection(field, outputType);
        if (isConnection) {
            outputType = getGraphQLConnection(field, outputType, ConnectionUtil.getRelay(field, container), container.getTypeRegistry());
            builder.arguments(ConnectionUtil.getRelay(field, container).getConnectionFieldArguments());
        }

        DataFetcher dataFetcher = new FieldDataFetcherBuilder(field, dataFetcherConstructor, outputType, typeFunction, container, isConnection).build();
        builder.type((GraphQLOutputType) outputType).description(new DescriptionBuilder(field).build())
                .deprecate(new DeprecateBuilder(field).build());

        container.getCodeRegistryBuilder().dataFetcher(coordinates(parentName, fieldName), dataFetcher);

        GraphQLDirective[] graphQLDirectives = new DirectivesBuilder(field, container).build();
        builder.withDirectives(graphQLDirectives);

        return builder.build();
    }

    public GraphQLInputObjectField getInputField(Method method, ProcessingElementsContainer container, String parentName) throws GraphQLAnnotationsException {
        GraphQLInputObjectField.Builder builder = newInputObjectField();
        builder.name(new MethodNameBuilder(method).alwaysPrettify(alwaysPrettify).build());
        TypeFunction typeFunction = getTypeFunction(method, container);
        GraphQLInputType inputType = (GraphQLInputType) new MethodTypeBuilder(method, typeFunction, container, true).build();
        builder.withDirectives(new DirectivesBuilder(method, container).build());
        return builder.type(inputType)
                .description(new DescriptionBuilder(method).build()).build();
    }

    public GraphQLInputObjectField getInputField(Field field, ProcessingElementsContainer container, String parentName) throws GraphQLAnnotationsException {
        GraphQLInputObjectField.Builder builder = newInputObjectField();
        builder.name(new FieldNameBuilder(field).alwaysPrettify(alwaysPrettify).build());
        TypeFunction typeFunction = getTypeFunction(field, container);
        GraphQLType graphQLType = typeFunction.buildType(true, field.getType(), field.getAnnotatedType(), container);
        builder.withDirectives(new DirectivesBuilder(field, container).build());
        return builder.type((GraphQLInputType) graphQLType)
                .description(new DescriptionBuilder(field).build()).build();
    }

    private GraphQLFieldDefinition handleRelayArguments(Method method, ProcessingElementsContainer container, GraphQLFieldDefinition.Builder builder, GraphQLOutputType outputType, List<GraphQLArgument> args) {
        GraphQLFieldDefinition relayFieldDefinition = null;
        if (method.isAnnotationPresent(GraphQLRelayMutation.class)) {
            relayFieldDefinition = buildRelayMutation(method, container, builder, outputType, args);

            // Getting the data fetcher from the old field type and putting it as the new type
            String newParentType = (getName(relayFieldDefinition.getType()));
            relayFieldDefinition.getType().getChildren().forEach(field -> {
                DataFetcher dataFetcher = CodeRegistryUtil.getDataFetcher(container.getCodeRegistryBuilder(), outputType, (GraphQLFieldDefinition) field);
                container.getCodeRegistryBuilder().dataFetcher(coordinates(newParentType, getName(field)), dataFetcher);
            });

        } else {
            builder.arguments(args);
        }
        return relayFieldDefinition;
    }

    private TypeFunction getTypeFunction(Method method, ProcessingElementsContainer container) {
        graphql.annotations.annotationTypes.GraphQLType annotation = method.getAnnotation(graphql.annotations.annotationTypes.GraphQLType.class);
        TypeFunction typeFunction = container.getDefaultTypeFunction();

        if (annotation != null) {
            typeFunction = newInstance(annotation.value());
        }
        return typeFunction;
    }

    private GraphQLFieldDefinition buildRelayMutation(Method method, ProcessingElementsContainer container, GraphQLFieldDefinition.Builder builder, GraphQLOutputType outputType, List<GraphQLArgument> args) {
        GraphQLFieldDefinition relayFieldDefinition;
        if (!(outputType instanceof GraphQLObjectType || outputType instanceof GraphQLInterfaceType)) {
            throw new RuntimeException("outputType should be an object or an interface");
        }
        StringBuilder titleBuffer = new StringBuilder(method.getName());
        titleBuffer.setCharAt(0, Character.toUpperCase(titleBuffer.charAt(0)));
        String title = titleBuffer.toString();
        List<GraphQLFieldDefinition> fieldDefinitions = outputType instanceof GraphQLObjectType ?
                ((GraphQLObjectType) outputType).getFieldDefinitions() :
                ((GraphQLInterfaceType) outputType).getFieldDefinitions();
        relayFieldDefinition = container.getRelay().mutationWithClientMutationId(title, method.getName(),
                args.stream().
                        map(t -> newInputObjectField().name(t.getName()).type(t.getType()).description(t.getDescription()).build()).
                        collect(Collectors.toList()), fieldDefinitions, new StaticDataFetcher(null));
        builder.arguments(relayFieldDefinition.getArguments()).type(relayFieldDefinition.getType());
        return relayFieldDefinition;
    }


    private TypeFunction getTypeFunction(Field field, ProcessingElementsContainer container) {
        graphql.annotations.annotationTypes.GraphQLType annotation = field.getAnnotation(graphql.annotations.annotationTypes.GraphQLType.class);

        TypeFunction typeFunction = container.getDefaultTypeFunction();

        if (annotation != null) {
            typeFunction = newInstance(annotation.value());
        }
        return typeFunction;
    }

    private GraphQLOutputType getGraphQLConnection(AccessibleObject field, graphql.schema.GraphQLType type, Relay relay, Map<String, graphql.schema.GraphQLType> typeRegistry) {
        if (type instanceof GraphQLNonNull) {
            GraphQLList listType = (GraphQLList) ((GraphQLNonNull) type).getWrappedType();
            return new GraphQLNonNull(internalGetGraphQLConnection(field, listType, relay, typeRegistry));
        } else {
            return internalGetGraphQLConnection(field, (GraphQLList) type, relay, typeRegistry);
        }
    }

    private GraphQLOutputType internalGetGraphQLConnection(AccessibleObject field, GraphQLList listType, Relay relay, Map<String, graphql.schema.GraphQLType> typeRegistry) {
        GraphQLType wrappedType = listType.getWrappedType();
        String connectionName = field.getAnnotation(GraphQLConnection.class).name();
        connectionName = connectionName.isEmpty() ? getName(wrappedType) : connectionName;
        GraphQLObjectType edgeType = getActualType(relay.edgeType(connectionName, (GraphQLOutputType) wrappedType, null, Collections.emptyList()), typeRegistry);
        return getActualType(relay.connectionType(connectionName, edgeType, Collections.emptyList()), typeRegistry);
    }

    private GraphQLObjectType getActualType(GraphQLObjectType type, Map<String, graphql.schema.GraphQLType> typeRegistry) {
        if (typeRegistry.containsKey(type.getName())) {
            type = (GraphQLObjectType) typeRegistry.get(type.getName());
        } else {
            typeRegistry.put(type.getName(), type);
        }
        return type;
    }

    public void setAlwaysPrettify(boolean alwaysPrettify) {
        this.alwaysPrettify = alwaysPrettify;
    }

    public void setDataFetcherConstructor(DataFetcherConstructor dataFetcherConstructor) {
        this.dataFetcherConstructor = dataFetcherConstructor;
    }

    public void unsetDataFetcherConstructor(DataFetcherConstructor dataFetcherConstructor) {
        this.dataFetcherConstructor = new DataFetcherConstructor();
    }
}
