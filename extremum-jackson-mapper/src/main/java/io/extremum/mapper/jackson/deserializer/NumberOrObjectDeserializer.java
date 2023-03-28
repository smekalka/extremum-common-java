package io.extremum.mapper.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.extremum.sharedmodels.basic.NumberOrObject;

import java.io.IOException;

public class NumberOrObjectDeserializer extends StdDeserializer<NumberOrObject<? extends Number, ?>> implements ResolvableDeserializer {

    private static final long serialVersionUID = 1L;

    private JsonDeserializer<?> contentDeserializer;
    private final JavaType valueType;

    public NumberOrObjectDeserializer(JavaType valueType) {
        super(valueType);
        this.valueType = valueType;
    }

    @Override
    public NumberOrObject<? extends Number, ?> deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        TreeNode node = parser.getCodec().readTree(parser);
        if (node instanceof NumericNode)
            return new NumberOrObject<>(((NumericNode) node).asDouble());
        else if (contentDeserializer != null && node instanceof ObjectNode) {
            JsonParser nodeParser = node.traverse();
            nodeParser.setCodec(parser.getCodec());
            nodeParser.nextToken();
            Object contents = contentDeserializer.deserialize(nodeParser, ctx);
            return new NumberOrObject<>(contents);
        } else if (contentDeserializer == null)
            throw MismatchedInputException.from(parser, NumberOrObject.class, "Content deserializer not resolved for value type " + valueType);
        else
            throw MismatchedInputException.from(parser, NumberOrObject.class, "Invalid node type: " + node);
    }

    @Override
    public void resolve(DeserializationContext ctx) throws JsonMappingException {
        JavaType containedType = valueType.containedTypeOrUnknown(0);
        contentDeserializer = ctx.findRootValueDeserializer(containedType);
    }

    @Override
    public NumberOrObject<? extends Number, ?> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return null;
    }

}
