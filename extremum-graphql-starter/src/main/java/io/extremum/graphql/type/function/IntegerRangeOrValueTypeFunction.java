package io.extremum.graphql.type.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.schema.*;
import io.extremum.graphql.type.function.util.JsonCoercingUtil;
import io.extremum.sharedmodels.structs.IntegerRangeOrValue;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.AnnotatedType;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

@Component
@AllArgsConstructor
public class IntegerRangeOrValueTypeFunction implements ScalarTypeFunction {

    private static GraphQLScalarType type;
    private final ObjectMapper objectMapper;

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return aClass == IntegerRangeOrValue.class;
    }

    @Override
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        return getType();
    }

    @Override
    public GraphQLScalarType getType() {
        if (type == null) {
            type = GraphQLScalarType.newScalar().name("IntegerRangeOrValue").coercing(
                    new Coercing<IntegerRangeOrValue, Object>() {
                        @SneakyThrows
                        @Override
                        public Object serialize(@NotNull Object dataFetcherResult) throws CoercingSerializeException {
                            return dataFetcherResult;
                        }

                        @NotNull
                        @Override
                        public IntegerRangeOrValue parseValue(@NotNull Object input) throws CoercingParseValueException {
                            return JsonCoercingUtil.parseValue(input, IntegerRangeOrValue.class, objectMapper);
                        }

                        @NotNull
                        @Override
                        public IntegerRangeOrValue parseLiteral(@NotNull Object input) throws CoercingParseLiteralException {
                            Object parsed = JsonCoercingUtil.parseLiteral(input, Collections.emptyMap());
                            if (parsed instanceof BigInteger) {
                                return new IntegerRangeOrValue(((BigInteger) parsed).intValue());
                            }
                            if (parsed instanceof Map) {
                                return new IntegerRangeOrValue(((BigInteger) ((Map<?, ?>) parsed).get("min")).intValue(), ((BigInteger) ((Map<?, ?>) parsed).get("max")).intValue());
                            }

                            throw new IllegalArgumentException(String.format("Cannot parse literal %s to %s", input, IntegerRangeOrValue.class.getSimpleName()));
                        }
                    }
            ).build();
        }
        return type;
    }
}