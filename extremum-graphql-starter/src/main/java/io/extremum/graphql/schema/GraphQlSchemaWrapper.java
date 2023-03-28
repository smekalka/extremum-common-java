package io.extremum.graphql.schema;

import graphql.com.google.common.collect.ImmutableList;
import graphql.com.google.common.collect.ImmutableMap;
import graphql.language.SchemaDefinition;
import graphql.language.SchemaExtensionDefinition;
import graphql.schema.*;
import graphql.schema.visibility.GraphqlFieldVisibility;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class GraphQlSchemaWrapper extends GraphQLSchema {

    @Setter
    @Getter
    private GraphQLSchema graphQLSchema;

    public GraphQlSchemaWrapper(BuilderWithoutTypes builder) {
        super(builder);
    }

    public GraphQlSchemaWrapper(GraphQLSchema existingSchema, GraphQLCodeRegistry codeRegistry, ImmutableMap<String, GraphQLNamedType> typeMap, ImmutableMap<String, ImmutableList<GraphQLObjectType>> interfaceNameToObjectTypes) {
        super(existingSchema, codeRegistry, typeMap, interfaceNameToObjectTypes);
    }

    @Override
    public GraphQLCodeRegistry getCodeRegistry() {
        return graphQLSchema.getCodeRegistry();
    }

    @Override
    public GraphQLFieldDefinition getIntrospectionSchemaFieldDefinition() {
        return graphQLSchema.getIntrospectionSchemaFieldDefinition();
    }

    @Override
    public GraphQLFieldDefinition getIntrospectionTypeFieldDefinition() {
        return graphQLSchema.getIntrospectionTypeFieldDefinition();
    }

    @Override
    public GraphQLFieldDefinition getIntrospectionTypenameFieldDefinition() {
        return graphQLSchema.getIntrospectionTypenameFieldDefinition();
    }

    @Override
    public GraphQLObjectType getIntrospectionSchemaType() {
        return graphQLSchema.getIntrospectionSchemaType();
    }

    @Override
    public Set<GraphQLType> getAdditionalTypes() {
        return graphQLSchema.getAdditionalTypes();
    }

    @Override
    public @Nullable GraphQLType getType(@NotNull String typeName) {
        return graphQLSchema.getType(typeName);
    }

    @Override
    public <T extends GraphQLType> List<T> getTypes(Collection<String> typeNames) {
        return graphQLSchema.getTypes(typeNames);
    }

    @Override
    public <T extends GraphQLType> T getTypeAs(String typeName) {
        return graphQLSchema.getTypeAs(typeName);
    }

    @Override
    public boolean containsType(String typeName) {
        return graphQLSchema.containsType(typeName);
    }

    @Override
    public GraphQLObjectType getObjectType(String typeName) {
        return graphQLSchema.getObjectType(typeName);
    }

    @Override
    public GraphQLFieldDefinition getFieldDefinition(FieldCoordinates fieldCoordinates) {
        return graphQLSchema.getFieldDefinition(fieldCoordinates);
    }

    @Override
    public Map<String, GraphQLNamedType> getTypeMap() {
        return graphQLSchema.getTypeMap();
    }

    @Override
    public List<GraphQLNamedType> getAllTypesAsList() {
        return graphQLSchema.getAllTypesAsList();
    }

    @Override
    public List<GraphQLNamedSchemaElement> getAllElementsAsList() {
        return graphQLSchema.getAllElementsAsList();
    }

    @Override
    public List<GraphQLObjectType> getImplementations(GraphQLInterfaceType type) {
        return graphQLSchema.getImplementations(type);
    }

    @Override
    public boolean isPossibleType(GraphQLNamedType abstractType, GraphQLObjectType concreteType) {
        return graphQLSchema.isPossibleType(abstractType, concreteType);
    }

    @Override
    public GraphQLObjectType getQueryType() {
        return graphQLSchema.getQueryType();
    }

    @Override
    public GraphQLObjectType getMutationType() {
        return graphQLSchema.getMutationType();
    }

    @Override
    public GraphQLObjectType getSubscriptionType() {
        return graphQLSchema.getSubscriptionType();
    }

    @Override
    @Deprecated
    public GraphqlFieldVisibility getFieldVisibility() {
        return graphQLSchema.getFieldVisibility();
    }

    @Override
    public List<GraphQLDirective> getDirectives() {
        return graphQLSchema.getDirectives();
    }

    @Override
    public Map<String, GraphQLDirective> getDirectivesByName() {
        return graphQLSchema.getDirectivesByName();
    }

    @Override
    public GraphQLDirective getDirective(String directiveName) {
        return graphQLSchema.getDirective(directiveName);
    }

    @Override
    @Deprecated
    public List<GraphQLDirective> getSchemaDirectives() {
        return graphQLSchema.getSchemaDirectives();
    }

    @Override
    @Deprecated
    public Map<String, GraphQLDirective> getSchemaDirectiveByName() {
        return graphQLSchema.getSchemaDirectiveByName();
    }

    @Override
    @Deprecated
    public Map<String, List<GraphQLDirective>> getAllSchemaDirectivesByName() {
        return graphQLSchema.getAllSchemaDirectivesByName();
    }

    @Override
    @Deprecated
    public GraphQLDirective getSchemaDirective(String directiveName) {
        return graphQLSchema.getSchemaDirective(directiveName);
    }

    @Override
    @Deprecated
    public List<GraphQLDirective> getSchemaDirectives(String directiveName) {
        return graphQLSchema.getSchemaDirectives(directiveName);
    }

    @Override
    public List<GraphQLAppliedDirective> getSchemaAppliedDirectives() {
        return graphQLSchema.getSchemaAppliedDirectives();
    }

    @Override
    public Map<String, List<GraphQLAppliedDirective>> getAllSchemaAppliedDirectivesByName() {
        return graphQLSchema.getAllSchemaAppliedDirectivesByName();
    }

    @Override
    public GraphQLAppliedDirective getSchemaAppliedDirective(String directiveName) {
        return graphQLSchema.getSchemaAppliedDirective(directiveName);
    }

    @Override
    public List<GraphQLAppliedDirective> getSchemaAppliedDirectives(String directiveName) {
        return graphQLSchema.getSchemaAppliedDirectives(directiveName);
    }

    @Override
    public @Nullable SchemaDefinition getDefinition() {
        return graphQLSchema.getDefinition();
    }

    @Override
    public List<SchemaExtensionDefinition> getExtensionDefinitions() {
        return graphQLSchema.getExtensionDefinitions();
    }

    @Override
    public boolean isSupportingMutations() {
        return graphQLSchema.isSupportingMutations();
    }

    @Override
    public boolean isSupportingSubscriptions() {
        return graphQLSchema.isSupportingSubscriptions();
    }

    @Override
    @Nullable
    public String getDescription() {
        return graphQLSchema.getDescription();
    }

    @Override
    public GraphQLSchema transform(Consumer<Builder> builderConsumer) {
        return graphQLSchema.transform(builderConsumer);
    }

    @Override
    public GraphQLSchema transformWithoutTypes(Consumer<BuilderWithoutTypes> builderConsumer) {
        return graphQLSchema.transformWithoutTypes(builderConsumer);
    }

    public static Builder newSchema() {
        return GraphQLSchema.newSchema();
    }

    public static Builder newSchema(GraphQLSchema existingSchema) {
        return GraphQLSchema.newSchema(existingSchema);
    }
}
