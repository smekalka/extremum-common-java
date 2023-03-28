package io.extremum.mapper.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.extremum.mapper.jackson.exceptions.DeserializationException;
import io.extremum.sharedmodels.structs.DurationVariativeValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DurationVariativeValueDeserializer extends StdDeserializer<DurationVariativeValue> {
    public DurationVariativeValueDeserializer() {
        super(DurationVariativeValue.class);
    }

    @Override
    public DurationVariativeValue deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        TreeNode tree = p.getCodec().readTree(p);

        if (tree instanceof ObjectNode) {
            TreeNode minTreeNode = tree.get(DurationVariativeValue.FIELDS.min.name());
            TreeNode maxTreeNode = tree.get(DurationVariativeValue.FIELDS.max.name());

            Map<String, String> errors = new HashMap<>();

            if (minTreeNode == null) {
                errors.put("duration[" + DurationVariativeValue.FIELDS.min.name() + "]", "Can't be null");
            }

            if (maxTreeNode == null) {
                errors.put("duration[" + DurationVariativeValue.FIELDS.max.name() + "]", "Can't be null");
            }

            if (!errors.isEmpty()) {
                throw new DeserializationException(errors);
            }

            Objects.requireNonNull(minTreeNode, "minTreeNode is null");
            Objects.requireNonNull(maxTreeNode, "maxTreeNode is null");

            return new DurationVariativeValue(minTreeNode.toString(), maxTreeNode.toString());
        } else if (tree instanceof TextNode) {
            return new DurationVariativeValue(((TextNode) tree).textValue());
        } else if (tree instanceof IntNode) {
            return new DurationVariativeValue(((IntNode) tree).intValue());
        } else {
            throw new DeserializationException("DurationVariativeValue", "Must be integer or string or object");
        }
    }
}
