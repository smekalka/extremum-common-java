package io.extremum.graphql.schema.builder;

import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.CannotCastMemberException;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.retrievers.GraphQLExtensionsHandler;
import graphql.annotations.processor.retrievers.GraphQLFieldRetriever;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;
import graphql.annotations.processor.searchAlgorithms.SearchAlgorithm;
import graphql.schema.GraphQLFieldDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static graphql.annotations.processor.util.ObjectUtil.getAllFields;

public class EnchancedGraphQLExtensionsHandler extends GraphQLExtensionsHandler {

    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private SearchAlgorithm fieldSearchAlgorithm;
    private SearchAlgorithm methodSearchAlgorithm;
    private GraphQLFieldRetriever fieldRetriever;

    public List<GraphQLFieldDefinition> getExtensionFields(Class<?> object, List<String> definedFields, ProcessingElementsContainer container) throws CannotCastMemberException {
        List<GraphQLFieldDefinition> fields = new ArrayList<>();
        if (container.getExtensionsTypeRegistry().containsKey(object)) {
            for (Class<?> aClass : container.getExtensionsTypeRegistry().get(object)) {
                for (Method method : graphQLObjectInfoRetriever.getOrderedMethods(aClass)) {
                    if (method.isBridge() || method.isSynthetic()) {
                        continue;
                    }
                    if (methodSearchAlgorithm.isFound(method)) {
                        addExtensionField(fieldRetriever.getField(object.getSimpleName(), method, container), fields, definedFields);
                    }
                }
                for (Field field : getAllFields(aClass).values()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    if (fieldSearchAlgorithm.isFound(field)) {
                        addExtensionField(fieldRetriever.getField(object.getSimpleName(), field, container), fields, definedFields);
                    }
                }
            }
        }
        return fields;
    }

    private void addExtensionField(GraphQLFieldDefinition gqlField, List<GraphQLFieldDefinition> fields, List<String> definedFields) {
        if (!definedFields.contains(gqlField.getName())) {
            definedFields.add(gqlField.getName());
            fields.add(gqlField);
        } else {
            definedFields.add(gqlField.getName());
            fields.add(gqlField);
        }
    }


    public void registerTypeExtension(Class<?> objectClass, ProcessingElementsContainer container) {
        GraphQLTypeExtension typeExtension = objectClass.getAnnotation(GraphQLTypeExtension.class);
        if (typeExtension == null) {
            throw new GraphQLAnnotationsException("Class is not annotated with GraphQLTypeExtension", null);
        } else {
            Class<?> aClass = typeExtension.value();
            if (!container.getExtensionsTypeRegistry().containsKey(aClass)) {
                container.getExtensionsTypeRegistry().put(aClass, new HashSet<>());
            }
            container.getExtensionsTypeRegistry().get(aClass).add(objectClass);
        }
    }

    public void setGraphQLObjectInfoRetriever(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
    }

    public void unsetGraphQLObjectInfoRetriever(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever) {
        this.graphQLObjectInfoRetriever = null;
    }


    public void setFieldSearchAlgorithm(SearchAlgorithm fieldSearchAlgorithm) {
        this.fieldSearchAlgorithm = fieldSearchAlgorithm;
    }

    public void unsetFieldSearchAlgorithm(SearchAlgorithm fieldSearchAlgorithm) {
        this.fieldSearchAlgorithm = null;
    }

    public void setMethodSearchAlgorithm(SearchAlgorithm methodSearchAlgorithm) {
        this.methodSearchAlgorithm = methodSearchAlgorithm;
    }

    public void unsetMethodSearchAlgorithm(SearchAlgorithm methodSearchAlgorithm) {
        this.methodSearchAlgorithm = null;
    }

    public void setFieldRetriever(GraphQLFieldRetriever fieldRetriever) {
        this.fieldRetriever = fieldRetriever;
    }

    public void unsetFieldRetriever(GraphQLFieldRetriever fieldRetriever) {
        this.fieldRetriever = null;
    }
}
