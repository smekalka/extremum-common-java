package io.extremum.graphql.type.function;

import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UUIDTypeFunctionTest {

    @Test
    @DisplayName("Gets proper type")
    void gets_proper_type() {
        UUIDTypeFunction typeFunction = new UUIDTypeFunction(new SystemJsonObjectMapper(DescriptorFactory::new));
        assertTrue(typeFunction.canBuildType(UUID.class, null));
        assertEquals("uuid", typeFunction.getType().getName());
        Coercing<?, ?> coercing = typeFunction.getType().getCoercing();
        String uuidString = new UUID(12, 14).toString();
        UUID uuid = (UUID) coercing.parseLiteral(StringValue.of(uuidString));
        assertEquals(new UUID(12, 14), uuid);
        assertEquals(uuid, coercing.serialize(uuid));

        Assertions.assertThrows(IllegalArgumentException.class, () -> coercing.parseLiteral(IntValue.of(1)));
    }
}