package io.extremum.graphql.type.function;

import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class URITypeFunctionTest {

    @Test
    @DisplayName("Gets proper type")
    void gets_proper_type() {
        URITypeFunction typeFunction = new URITypeFunction(new SystemJsonObjectMapper(DescriptorFactory::new));
        assertTrue(typeFunction.canBuildType(URI.class, null));
        assertEquals("uri", typeFunction.getType().getName());
        Coercing<?, ?> coercing = typeFunction.getType().getCoercing();
        String uriString = "https://extremum.io";
        URI uri = (URI) coercing.parseLiteral(StringValue.of(uriString));
        assertEquals("https://extremum.io", uri.toString());
        assertEquals(uri, coercing.serialize(uri));

        Assertions.assertThrows(IllegalArgumentException.class, () -> coercing.parseLiteral(IntValue.of(1)));

    }
}