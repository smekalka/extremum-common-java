package io.extremum.graphql.type.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.schema.*;
import io.extremum.graphql.type.function.util.JsonCoercingUtil;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.AnnotatedType;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class StringOrMultilingualTypeFunction implements ScalarTypeFunction {

    private static GraphQLScalarType type;
    private final Locale locale;
    private final ObjectMapper objectMapper;

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return aClass == StringOrMultilingual.class;
    }

    @Override
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        return getType();
    }

    @Override
    public GraphQLScalarType getType() {
        if (type == null) {
            type = GraphQLScalarType.newScalar().name("StringOrMultilingual").coercing(
                    new Coercing<StringOrMultilingual, Object>() {
                        @Override
                        @SneakyThrows
                        public Object serialize(@NotNull Object dataFetcherResult) throws CoercingSerializeException {
                            return dataFetcherResult;
                        }

                        @NotNull
                        @Override
                        @SneakyThrows
                        public StringOrMultilingual parseValue(@NotNull Object input) throws CoercingParseValueException {
                            return JsonCoercingUtil.parseValue(input, StringOrMultilingual.class, objectMapper);
                        }

                        @NotNull
                        @Override
                        public StringOrMultilingual parseLiteral(@NotNull Object input) throws CoercingParseLiteralException {
                            if (input instanceof StringValue) {
                                return new StringOrMultilingual(((StringValue) input).getValue());
                            }

                            if (input instanceof ObjectValue) {
                                Map<MultilingualLanguage, String> collect = ((ObjectValue) input).getObjectFields()
                                        .stream()
                                        .collect(
                                                Collectors.toMap(
                                                        objectField -> MultilingualLanguage.valueOf(objectField.getName()),
                                                        objectField -> ((StringValue) (objectField.getValue())).getValue()
                                                ));

                                return new StringOrMultilingual(collect, locale);
                            }

                            throw new IllegalArgumentException(String.format("Cannot parse literal %s to %s", input, StringOrMultilingual.class.getSimpleName()));
                        }
                    }
            ).build();
        }

        return type;
    }
}