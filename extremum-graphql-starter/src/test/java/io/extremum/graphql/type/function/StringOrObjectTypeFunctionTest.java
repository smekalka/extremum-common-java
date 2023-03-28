package io.extremum.graphql.type.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.sharedmodels.basic.StringOrObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringOrObjectTypeFunctionTest {

    @Test
    @DisplayName("Gets proper type")
    void gets_proper_type() {
        StringOrObjectTypeFunction typeFunction = new StringOrObjectTypeFunction(new SystemJsonObjectMapper(DescriptorFactory::new));
        assertTrue(typeFunction.canBuildType(StringOrObject.class, null));
        assertEquals("StringOrObject", typeFunction.getType().getName());
        Coercing<?, ?> coercing = typeFunction.getType().getCoercing();
        StringOrObject<?> value = (StringOrObject<?>) coercing.parseLiteral(StringValue.of("Extreмум"));
        assertEquals("Extreмум", value.getString());
        assertTrue(value.isSimple());

        value = (StringOrObject<?>) coercing.parseLiteral(
                ObjectValue.newObjectValue()
                        .objectField(
                                ObjectField
                                        .newObjectField()
                                        .name("name")
                                        .value(StringValue.of("Extremum"))
                                        .build()
                        )
                        .build()
        );

        assertEquals("Extremum", new ObjectMapper().convertValue(value.getObject(), ComplexObject.class).getName());
        assertEquals(new StringOrObject<>("Экстремум"), coercing.serialize((new StringOrObject<>("Экстремум"))));
    }


    @NoArgsConstructor
    @Data
    private static class ComplexObject {
        private String name;
    }
}