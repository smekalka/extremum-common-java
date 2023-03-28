package io.extremum.mapper.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.extremum.sharedmodels.basic.IntegerOrString;

import java.io.IOException;

/**
 * @author rpuch
 */
public class IntegerOrStringDeserializer extends StdDeserializer<IntegerOrString> {
    public IntegerOrStringDeserializer() {
        super(IntegerOrString.class);
    }

    @Override
    public IntegerOrString deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        TreeNode node = jsonParser.getCodec().readTree(jsonParser);

        if (node == null) {
            return null;
        }
        if (!node.isValueNode()) {
            throw MismatchedInputException.from(jsonParser, IntegerOrString.class, "A value was expected");
        }

        if (!(node instanceof JsonNode)) {
            throw MismatchedInputException.from(jsonParser, IntegerOrString.class, "Only JsonNode nodes are supported, but we got " + node.getClass());
        }
        JsonNode jsonNode = (JsonNode) node;

        if (node.numberType() == JsonParser.NumberType.INT) {
            return new IntegerOrString(jsonNode.intValue());
        }
        if (jsonNode.textValue() != null) {
            return new IntegerOrString(jsonNode.textValue());
        }

        throw MismatchedInputException.from(jsonParser, IntegerOrString.class, "Cannot deserialize: it's not empty, not a string and not an integer");
    }
}
