package io.extremum.graphql.type.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.language.StringValue;
import graphql.schema.*;
import io.extremum.graphql.type.function.util.JsonCoercingUtil;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.AnnotatedType;

@Component
@AllArgsConstructor
public class DescriptorTypeFunction implements ScalarTypeFunction {

    private static GraphQLScalarType type;
    private final ObjectMapper objectMapper;

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return aClass == Descriptor.class;
    }

    @Override
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        return getType();
    }

    @Override
    public GraphQLScalarType getType() {
        if (type == null) {
            type = GraphQLScalarType.newScalar().name("Descriptor").coercing(
                    new Coercing<Descriptor, Descriptor>() {
                        @Override
                        @SneakyThrows
                        public Descriptor serialize(@NotNull Object dataFetcherResult) throws CoercingSerializeException {
                            return (Descriptor) dataFetcherResult;
                        }

                        @NotNull
                        @Override
                        public Descriptor parseValue(@NotNull Object input) throws CoercingParseValueException {
                            return JsonCoercingUtil.parseValue(input, Descriptor.class, objectMapper);
                        }

                        @NotNull
                        @Override
                        public Descriptor parseLiteral(@NotNull Object input) throws CoercingParseLiteralException {
                            if (input instanceof StringValue) {
                                return new Descriptor(((StringValue) input).getValue());
                            }

                            throw new IllegalArgumentException(String.format("Cannot parse literal %s to %s", input, Descriptor.class.getSimpleName()));
                        }
                    }
            ).build();
        }
        return type;
    }
}