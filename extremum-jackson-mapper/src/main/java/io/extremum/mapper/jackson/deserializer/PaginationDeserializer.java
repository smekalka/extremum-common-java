package io.extremum.mapper.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.extremum.sharedmodels.dto.Pagination;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * @author rpuch
 */
public class PaginationDeserializer extends StdDeserializer<Pagination> {
    private final ObjectMapper mapper;

    public PaginationDeserializer(ObjectMapper mapper) {
        super(Pagination.class);
        this.mapper = mapper;
    }

    @Override
    public Pagination deserialize(JsonParser parser,
            DeserializationContext context) throws IOException {
        TreeNode node = parser.getCodec().readTree(parser);

        if (node == null) {
            return null;
        }

        return Pagination.builder()
                .count(getInt("count", node, parser))
                .total(getOptionalLong("total", node, parser))
                .offset(getInt("offset", node, parser))
                .since(getObject("since", ZonedDateTime.class, node))
                .until(getObject("until", ZonedDateTime.class, node))
                .build();
    }

    private int getInt(String fieldName, TreeNode node, JsonParser parser) throws JsonParseException {
        TreeNode fieldNode = node.get(fieldName);
        if (fieldNode == null) {
            throw new JsonParseException(parser, String.format("'%s' field is required for Pagination", fieldName));
        }

        if (!(fieldNode instanceof JsonNode)) {
            throw new JsonParseException(parser, String.format("'%s' must be an integer", fieldName));
        }

        JsonNode jsonNode = (JsonNode) fieldNode;
        return jsonNode.intValue();
    }

    private Long getOptionalLong(String fieldName, TreeNode node, JsonParser parser) throws JsonParseException {
        TreeNode fieldNode = node.get(fieldName);
        if (fieldNode == null) {
            return null;
        }

        if (!(fieldNode instanceof JsonNode)) {
            throw new JsonParseException(parser, String.format("'%s' must be an integer", fieldName));
        }

        JsonNode jsonNode = (JsonNode) fieldNode;
        return jsonNode.longValue();
    }

    private <T> T getObject(String fieldName, Class<T> valueClass, TreeNode node) throws JsonProcessingException {
        TreeNode fieldNode = node.get(fieldName);
        if (fieldNode == null) {
            return null;
        }

        return mapper.treeToValue(fieldNode, valueClass);
    }
}
