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
import com.fasterxml.jackson.databind.node.TextNode;
import io.extremum.sharedmodels.basic.StringOrObject;

import java.io.IOException;

public class StringOrObjectDeserializer extends StdDeserializer<StringOrObject<?>> implements ResolvableDeserializer {

    private static final long serialVersionUID = 1L;

    private JsonDeserializer<?> contentDeserializer;
    private final JavaType valueType;

    StringOrObjectDeserializer(JavaType valueType) {
        super(valueType);
        this.valueType = valueType;
    }

    @Override
    public StringOrObject<?> deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        if (contentDeserializer == null)
            throw new IllegalStateException("Content deserializer not resolved for value type " + valueType);
        TreeNode node = parser.getCodec().readTree(parser);
        if (node instanceof TextNode)
            return new StringOrObject<>(((TextNode) node).asText());
        else if (contentDeserializer != null) {
            JsonParser nodeParser = node.traverse();
            nodeParser.setCodec(parser.getCodec());
            nodeParser.nextToken();
            Object contents = contentDeserializer.deserialize(nodeParser, ctx);
            return new StringOrObject<>(contents);
        } else
            throw MismatchedInputException.from(parser, StringOrObject.class, "Invalid node type: " + node);
    }

    @Override
    public void resolve(DeserializationContext ctx) throws JsonMappingException {
        JavaType containedType = valueType.containedTypeOrUnknown(0);
        contentDeserializer = ctx.findRootValueDeserializer(containedType);
    }

    @Override
    public StringOrObject<?> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return null;
    }

}
