package io.extremum.graphql.type.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.language.StringValue;
import graphql.schema.*;
import io.extremum.graphql.type.function.util.JsonCoercingUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.AnnotatedType;
import java.net.URI;

@Component
@AllArgsConstructor
public class URITypeFunction implements ScalarTypeFunction {

    private static GraphQLScalarType type;
    private final ObjectMapper objectMapper;

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return aClass == URI.class;
    }

    @Override
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        return getType();
    }

    @Override
    public GraphQLScalarType getType() {
        if (type == null) {
            type = GraphQLScalarType.newScalar().name("uri").coercing(
                    new Coercing<URI, URI>() {
                        @Override
                        public URI serialize(@NotNull Object dataFetcherResult) throws CoercingSerializeException {
                            return (URI) dataFetcherResult;
                        }

                        @NotNull
                        @Override
                        public URI parseValue(@NotNull Object input) throws CoercingParseValueException {
                            return JsonCoercingUtil.parseValue(input, URI.class, objectMapper);
                        }

                        @SneakyThrows
                        @NotNull
                        @Override
                        public URI parseLiteral(@NotNull Object input) throws CoercingParseLiteralException {
                            if (input instanceof StringValue) {
                                return new URI(((StringValue) input).getValue());
                            }

                            throw new IllegalArgumentException(String.format("Cannot parse literal %s to %s", input, URI.class.getSimpleName()));
                        }
                    }
            ).build();
        }
        return type;
    }
}