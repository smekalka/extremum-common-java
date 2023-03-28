package io.extremum.dynamic.validator.services.impl.networknt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ImpermanentValidationContext;
import com.networknt.schema.JsonValidator;
import com.networknt.schema.TypeValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;

class DefaultImpermanentValidationContextTest {
    @Test
    void isValidatorSupported_true() throws IOException {
        Set<String> accumulator = new HashSet<>();

        ImpermanentValidationContext ctx = new DefaultImpermanentValidationContext(accumulator);

        JsonValidator validator = mock(TypeValidator.class);
        JsonNode node = new ObjectMapper().readValue("\"text\"", JsonNode.class);
        JsonNode schemaNode = new ObjectMapper().readValue("{\"type\": \"string\", \"format\": \"extremum-date-time\"}", JsonNode.class);

        boolean validatorSupported = ctx.isValidatorSupported(validator, node, null, schemaNode, null);

        Assertions.assertTrue(validatorSupported);
    }

    @Test
    void isValidatorSupported_false() throws IOException {
        Set<String> accumulator = new HashSet<>();

        ImpermanentValidationContext ctx = new DefaultImpermanentValidationContext(accumulator);

        JsonValidator validator = mock(TypeValidator.class);
        JsonNode node = new ObjectMapper().readValue("\"text\"", JsonNode.class);
        JsonNode schemaNode = new ObjectMapper().readValue("{\"type\": \"string\"}", JsonNode.class);

        boolean validatorSupported = ctx.isValidatorSupported(validator, node, null, schemaNode, null);

        Assertions.assertFalse(validatorSupported);
    }

    @Test
    void applyContext() {
        Set<String> accumulator = new HashSet<>();

        ImpermanentValidationContext ctx = new DefaultImpermanentValidationContext(accumulator);
        ctx.applyContext(null, null, null, "$.data");

        Assertions.assertEquals(1, accumulator.size());
        Assertions.assertTrue(accumulator.contains("$.data"));
    }
}