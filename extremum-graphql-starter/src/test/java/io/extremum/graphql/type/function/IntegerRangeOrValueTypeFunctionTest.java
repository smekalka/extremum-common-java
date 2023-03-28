package io.extremum.graphql.type.function;

import graphql.language.IntValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.sharedmodels.structs.IntegerRangeOrValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegerRangeOrValueTypeFunctionTest {

    @Test
    @DisplayName("Gets proper type")
    void gets_proper_type() {
        IntegerRangeOrValueTypeFunction typeFunction = new IntegerRangeOrValueTypeFunction(new SystemJsonObjectMapper(DescriptorFactory::new));
        assertTrue(typeFunction.canBuildType(IntegerRangeOrValue.class, null));
        assertEquals("IntegerRangeOrValue", typeFunction.getType().getName());
        Coercing<?, ?> coercing = typeFunction.getType().getCoercing();
        IntegerRangeOrValue value = (IntegerRangeOrValue) coercing.parseLiteral(IntValue.of(12));
        assertEquals(12, value.value);
        assertTrue(value.isInteger());
        value = (IntegerRangeOrValue) coercing.parseLiteral(
                ObjectValue.newObjectValue()
                        .objectField(
                                ObjectField
                                        .newObjectField()
                                        .name("min")
                                        .value(IntValue.of(12))
                                        .build()
                        )
                        .objectField(
                                ObjectField
                                        .newObjectField()
                                        .name("max")
                                        .value(IntValue.of(14))
                                        .build()
                        )
                        .build()
        );
        assertEquals(12, value.min);
        assertEquals(14, value.max);
        assertTrue(value.isObject());

        IntegerRangeOrValue integerRangeOrValue = new IntegerRangeOrValue(12, 14);
        assertEquals(integerRangeOrValue, coercing.serialize(integerRangeOrValue));


        Assertions.assertThrows(IllegalArgumentException.class, () -> coercing.parseLiteral(StringValue.of("")));
    }
}