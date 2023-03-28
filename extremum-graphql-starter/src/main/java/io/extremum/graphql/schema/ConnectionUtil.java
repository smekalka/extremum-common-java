package io.extremum.graphql.schema;

import graphql.annotations.connection.ConnectionValidator;
import graphql.annotations.connection.FakeRelay;
import graphql.annotations.connection.GraphQLConnection;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.relay.Relay;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLUnionType;

import java.lang.reflect.AccessibleObject;
import java.util.Arrays;
import java.util.List;

import static graphql.annotations.processor.util.ReflectionKit.newInstance;

public class ConnectionUtil {
    private static final List<Class<?>> TYPES_FOR_CONNECTION = Arrays.asList(GraphQLScalarType.class, GraphQLObjectType.class, GraphQLInterfaceType.class, GraphQLUnionType.class, GraphQLTypeReference.class);

    public static boolean isConnection(AccessibleObject obj, GraphQLType type) {
        if (!obj.isAnnotationPresent(GraphQLConnection.class)) {
            return false;
        }

        if (type instanceof graphql.schema.GraphQLNonNull) {
            type = ((GraphQLNonNull) type).getWrappedType();
        }
        if (!(type instanceof GraphQLList)) {
            return false;
        }

        type = ((GraphQLList) type).getWrappedType();
        if (type instanceof graphql.schema.GraphQLNonNull) {
            type = ((GraphQLNonNull) type).getWrappedType();
        }

        final GraphQLType elementType = type;
        boolean isValidGraphQLTypeForConnection = TYPES_FOR_CONNECTION.stream().anyMatch(aClass -> aClass.isInstance(elementType));

        if (isValidGraphQLTypeForConnection) {
            ConnectionValidator validator = newInstance(obj.getAnnotation(GraphQLConnection.class).validator());
            validator.validate(obj);
            return true;
        } else {
            return false;
        }
    }

    public static Relay getRelay(AccessibleObject obj, ProcessingElementsContainer container) {
        if (obj.isAnnotationPresent(GraphQLConnection.class)) {
            Class<? extends Relay> aClass = obj.getAnnotation(GraphQLConnection.class).connectionType();
            if (!FakeRelay.class.isAssignableFrom(aClass)) {
                return newInstance(aClass);
            }
        }
        return container.getRelay();
    }
}
