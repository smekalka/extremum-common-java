package io.extremum.mapper.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.content.Display;
import io.extremum.sharedmodels.content.Media;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class DisplayDeserializer extends StdDeserializer<Display> {
    private ObjectMapper mapper;

    public DisplayDeserializer(ObjectMapper mapper) {
        super(Display.class);
        this.mapper = mapper;
    }

    @Override
    public Display deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        TreeNode tree = jsonParser.getCodec().readTree(jsonParser);

        if (tree == null) {
            return null;
        } else if (tree.isValueNode()) {
            return new Display(((TextNode) tree).textValue());
        } else {
            StringOrMultilingual caption = extractCaption(tree);
            Media icon = extractMedia(tree, Display.FIELDS.icon.name());
            Media splash = extractMedia(tree, Display.FIELDS.splash.name());

            return new Display(caption, icon, splash);
        }
    }

    private Media extractMedia(TreeNode tree, String key) {
        TreeNode mediaNode = tree.get(key);

        if (mediaNode == null) {
            return null;
        } else {
            try {
                return mapper.treeToValue(mediaNode, Media.class);
            } catch (JsonProcessingException e) {
                log.error("Unable to deserialize Display#{}", key, e);
                return null;
            }
        }
    }

    private StringOrMultilingual extractCaption(TreeNode tree) {
        TreeNode treeNode = tree.get(Display.FIELDS.caption.name());
        if (treeNode == null) {
            return null;
        } else {
            try {
                return mapper.treeToValue(treeNode, StringOrMultilingual.class);
            } catch (JsonProcessingException e) {
                log.error("Unable to deserialize Display#{}", Display.FIELDS.caption.name(), e);
                return null;
            }
        }
    }
}
