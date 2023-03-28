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
import java.util.UUID;

@Component
@AllArgsConstructor
public class UUIDTypeFunction implements ScalarTypeFunction {

    private static GraphQLScalarType type;
    private final ObjectMapper objectMapper;

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return aClass == UUID.class;
    }

    @Override
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        return getType();
    }

    @Override
    public GraphQLScalarType getType() {
        if (type == null) {
            type = GraphQLScalarType.newScalar().name("uuid").coercing(
                    new Coercing<UUID, UUID>() {
                        @Override
                        public UUID serialize(@NotNull Object dataFetcherResult) throws CoercingSerializeException {
                            return (UUID) dataFetcherResult;
                        }

                        @NotNull
                        @Override
                        public UUID parseValue(@NotNull Object input) throws CoercingParseValueException {
                            return JsonCoercingUtil.parseValue(input, UUID.class, objectMapper);
                        }

                        @NotNull
                        @Override
                        public UUID parseLiteral(@NotNull Object input) throws CoercingParseLiteralException {
                            if (input instanceof StringValue) {
                                return UUID.fromString(((StringValue) input).getValue());
                            }

                            throw new IllegalArgumentException(String.format("Cannot parse literal %s to %s", input, UUID.class.getSimpleName()));
                        }
                    }
            ).build();
        }
        return type;
    }
}