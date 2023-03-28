package io.extremum.dynamic.validator.services.impl.networknt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.networknt.schema.*;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.Set;

import static io.extremum.dynamic.DynamicModelConstants.CYBERNATED_DATE_TIME_FORMAT_NAME;

@RequiredArgsConstructor
public class DefaultImpermanentValidationContext implements ImpermanentValidationContext {
    private final Set<String> paths;

    @Override
    public boolean isValidatorSupported(JsonValidator validator, JsonNode node, JsonNode rootNode, JsonNode schemaNode, String at) {
        return TypeValidator.class.isAssignableFrom(validator.getClass()) &&
                TypeFactory.getValueNodeType(node).equals(JsonType.STRING) &&
                Optional.ofNullable(schemaNode.get("format"))
                        .filter(TextNode.class::isInstance)
                        .map(TextNode.class::cast)
                        .map(TextNode::textValue)
                        .filter(CYBERNATED_DATE_TIME_FORMAT_NAME::equals).isPresent();
    }

    @Override
    public void applyContext(JsonNode node, JsonNode rootNode, JsonNode schemaNode, String at) {
        paths.add(at);
    }
}
