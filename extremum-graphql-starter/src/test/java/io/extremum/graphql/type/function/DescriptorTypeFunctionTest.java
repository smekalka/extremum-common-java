package io.extremum.graphql.type.function;

import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DescriptorTypeFunctionTest {

    @Test
    @DisplayName("Gets proper type")
    void gets_proper_type() {

        DescriptorTypeFunction typeFunction = new DescriptorTypeFunction(new SystemJsonObjectMapper(DescriptorFactory::new));
        assertTrue(typeFunction.canBuildType(Descriptor.class, null));
        assertEquals("Descriptor", typeFunction.getType().getName());
        Coercing<?, ?> coercing = typeFunction.getType().getCoercing();
        Descriptor descriptor = (Descriptor) coercing.parseLiteral(StringValue.of("does-not-matter"));
        assertEquals(descriptor.getExternalId(), "does-not-matter");
        assertEquals(
                Descriptor.builder().externalId("does-not-matter").build().getExternalId(),
                ((Descriptor) coercing.serialize(Descriptor.builder().externalId("does-not-matter").build())).getExternalId()
        );

        Assertions.assertThrows(IllegalArgumentException.class, () -> coercing.parseLiteral(IntValue.of(1)));
    }
}