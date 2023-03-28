package io.extremum.graphql.type.function.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.language.*;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Slf4j
public
class JsonCoercingUtil {

    public static Object parseLiteral(Object input, Map<String, Object> variables) throws CoercingParseLiteralException {
        if (!(input instanceof Value)) {
            log.error("Expected 'Value', got: {}", input);
            throw new CoercingParseLiteralException("Expected 'Value', got: " + input);
        }
        Object result = null;
        if (input instanceof StringValue) {
            result = ((StringValue) input).getValue();
        } else if (input instanceof IntValue) {
            result = ((IntValue) input).getValue();
        } else if (input instanceof FloatValue) {
            result = ((FloatValue) input).getValue();
        } else if (input instanceof BooleanValue) {
            result = ((BooleanValue) input).isValue();
        } else if (input instanceof EnumValue) {
            result = ((EnumValue) input).getName();
        } else if (input instanceof VariableReference) {
            result = variables.get(((VariableReference) input).getName());
        } else if (input instanceof ArrayValue) {
            result = ((ArrayValue) input).getValues().stream()
                    .map(v -> parseLiteral(v, variables))
                    .collect(toList());
        } else if (input instanceof ObjectValue) {
            Map<String, Object> map = new HashMap<>();
            for (ObjectField f : ((ObjectValue) input).getObjectFields()) {
                if (map.put(f.getName(), parseLiteral(f.getValue(), variables)) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            }
            result = map;
        }
        return result;
    }

    @SneakyThrows
    public static <T> T parseValue(Object input, Class<T> tClass, ObjectMapper objectMapper) {
        try {
            return objectMapper.convertValue(input, tClass);
        } catch (RuntimeException e) {
            throw new CoercingParseValueException(objectMapper.writeValueAsString(input));
        }
    }
}

