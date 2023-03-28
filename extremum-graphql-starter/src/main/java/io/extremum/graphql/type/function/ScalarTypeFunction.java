package io.extremum.graphql.type.function;

import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.GraphQLScalarType;

public interface ScalarTypeFunction extends TypeFunction {

    GraphQLScalarType getType();
}
