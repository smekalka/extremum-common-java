package io.extremum.graphql.type.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import io.extremum.graphql.type.function.util.JsonCoercingUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.AnnotatedType;
import java.util.Map;

@Component
@AllArgsConstructor
public class MapTypeFunction implements ScalarTypeFunction {

    private static GraphQLScalarType type;
    private final ObjectMapper objectMapper;

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return aClass == Map.class;
    }

    @Override
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        return getType();
    }

    @Override
    public GraphQLScalarType getType() {
        if (type == null) {
            type = GraphQLScalarType.newScalar().name("Map").coercing(
                    new Coercing<Object, Map<?, ?>>() {
                        @Override
                        public Map<?, ?> serialize(@NotNull Object dataFetcherResult) throws CoercingSerializeException {
                            return (Map<?, ?>) dataFetcherResult;
                        }

                        @NotNull
                        @Override
                        public Map<?, ?> parseValue(@NotNull Object input) throws CoercingParseValueException {
                            return JsonCoercingUtil.parseValue(input, Map.class, objectMapper);
                        }

                        @SneakyThrows
                        @NotNull
                        @Override
                        public Map<?, ?> parseLiteral(@NotNull Object input) throws CoercingParseLiteralException {
                            throw new UnsupportedOperationException();
                        }
                    }
            ).build();
        }
        return type;
    }
}