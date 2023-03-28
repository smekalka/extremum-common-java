package io.extremum.graphql.type.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.schema.*;
import io.extremum.graphql.type.function.util.JsonCoercingUtil;
import io.extremum.sharedmodels.basic.StringOrObject;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.AnnotatedType;
import java.util.Collections;

@Component
public class StringOrObjectTypeFunction implements ScalarTypeFunction {

    private static GraphQLScalarType type;
    private final ObjectMapper objectMapper;

    public StringOrObjectTypeFunction(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return aClass == StringOrObject.class;
    }

    @Override
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        return getType();
    }

    @Override
    public GraphQLScalarType getType() {
        if (type == null) {
            type = GraphQLScalarType.newScalar().name("StringOrObject").coercing(
                    new Coercing<StringOrObject<?>, Object>() {
                        @SneakyThrows
                        @Override
                        public Object serialize(@NotNull Object dataFetcherResult) throws CoercingSerializeException {
                            return dataFetcherResult;
                        }

                        @NotNull
                        @Override
                        public StringOrObject<?> parseValue(@NotNull Object input) throws CoercingParseValueException {
                            return JsonCoercingUtil.parseValue(input, StringOrObject.class, objectMapper);
                        }

                        @NotNull
                        @Override
                        public StringOrObject<?> parseLiteral(@NotNull Object input) throws CoercingParseLiteralException {
                            Object parsed = JsonCoercingUtil.parseLiteral(input, Collections.emptyMap());
                            if (parsed instanceof String) {
                                return new StringOrObject<>((String) parsed);
                            } else {
                                return new StringOrObject<>(parsed);
                            }
                        }
                    }
            ).build();
        }
        return type;
    }
}