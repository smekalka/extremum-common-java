package io.extremum.graphql.type.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.language.StringValue;
import graphql.schema.*;
import io.extremum.graphql.type.function.util.JsonCoercingUtil;
import io.extremum.sharedmodels.structs.IntegerRangeOrValue;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.AnnotatedType;
import java.time.Instant;
import java.util.Date;

@Component
@AllArgsConstructor
public class DateTypeFunction implements ScalarTypeFunction {

    private static GraphQLScalarType type;
    private final ObjectMapper objectMapper;

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return aClass == Date.class;
    }

    @Override
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        return getType();
    }

    @Override
    public GraphQLScalarType getType() {
        if (type == null) {
            type = GraphQLScalarType.newScalar().name("Date").coercing(
                    new Coercing<Object, Date>() {
                        @Override
                        public Date serialize(@NotNull Object dataFetcherResult) throws CoercingSerializeException {
                            return (Date) dataFetcherResult;
                        }

                        @NotNull
                        @Override
                        public Date parseValue(@NotNull Object input) throws CoercingParseValueException {
                            return JsonCoercingUtil.parseValue(input, Date.class, objectMapper);
                        }

                        @NotNull
                        @Override
                        public Date parseLiteral(@NotNull Object input) throws CoercingParseLiteralException {
                            if (input instanceof StringValue) {
                                return Date.from(Instant.parse(((StringValue) input).getValue()));
                            }

                            throw new IllegalArgumentException(String.format("Cannot parse literal %s to %s", input, IntegerRangeOrValue.class.getSimpleName()));
                        }
                    }
            ).build();
        }
        return type;
    }
}