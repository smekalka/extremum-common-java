package io.extremum.mapper.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.extremum.mapper.jackson.exceptions.DeserializationException;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import io.extremum.sharedmodels.basic.StringOrMultilingual;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class StringOrMultilingualDeserializer extends StdDeserializer<StringOrMultilingual> {
    public StringOrMultilingualDeserializer() {
        super(StringOrMultilingual.class);
    }

    public StringOrMultilingualDeserializer(Locale locale) {
        super(StringOrMultilingual.class);
        this.locale = locale;
    }

    private Locale locale;

    @Override
    public StringOrMultilingual deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        TreeNode tree = parser.getCodec().readTree(parser);

        if (tree instanceof TextNode) {
            return new StringOrMultilingual(((TextNode) tree).textValue());
        }

        if (tree instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) tree;

            Map<String, String> errors = new HashMap<>();
            Map<MultilingualLanguage, String> multilingualMap = new HashMap<>();

            Iterator<Map.Entry<String, JsonNode>> fieldIterator = objectNode.fields();
            while (fieldIterator.hasNext()) {
                Map.Entry<String, JsonNode> field = fieldIterator.next();
                if (!field.getValue().isTextual()) {
                    throw MismatchedInputException.from(parser, StringOrMultilingual.class,
                            String.format("A non-textual node found for key '%s'", field.getKey()));
                }

                String key = field.getKey();
                MultilingualLanguage multilingual = MultilingualLanguage.fromString(key);

                if (multilingual == null) {
                    errors.put(key, "Invalid language. Use RFC 5646");
                }

                multilingualMap.put(multilingual, field.getValue().textValue());
            }

            if (!errors.isEmpty()) {
                throw new DeserializationException(errors);
            }

            return new StringOrMultilingual(multilingualMap, locale);
        } else {
            throw new DeserializationException("StringOrMultilingual", "must be in a simple text format of multilingual object");
        }
    }
}
