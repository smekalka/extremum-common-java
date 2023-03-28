package io.extremum.graphql.type.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.language.StringValue;
import graphql.schema.*;
import io.extremum.graphql.type.function.util.JsonCoercingUtil;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.AnnotatedType;
import java.time.ZonedDateTime;

@Component
@AllArgsConstructor
public class ZonedDateTimeTimeTypeFunction implements ScalarTypeFunction {

    private static GraphQLScalarType type;
    private final ObjectMapper objectMapper;

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return aClass == ZonedDateTime.class;
    }

    @Override
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        return getType();
    }

    @Override
    public GraphQLScalarType getType() {
        if (type == null) {
            type = GraphQLScalarType.newScalar().name("ZonedDateTime").coercing(
                    new Coercing<ZonedDateTime, ZonedDateTime>() {
                        @Override
                        public ZonedDateTime serialize(@NotNull Object dataFetcherResult) throws CoercingSerializeException {
                            return (ZonedDateTime) dataFetcherResult;
                        }

                        @NotNull
                        @Override
                        public ZonedDateTime parseValue(@NotNull Object input) throws CoercingParseValueException {
                            return JsonCoercingUtil.parseValue(input, ZonedDateTime.class, objectMapper);
                        }

                        @NotNull
                        @Override
                        public ZonedDateTime parseLiteral(@NotNull Object input) throws CoercingParseLiteralException {
                            if (input instanceof StringValue) {
                                return ZonedDateTime.parse(((StringValue) input).getValue());
                            }

                            throw new IllegalArgumentException(String.format("Cannot parse literal %s to %s", input, ZonedDateTime.class.getSimpleName()));
                        }
                    }
            ).build();
        }
        return type;
    }
}