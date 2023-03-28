package io.extremum.graphql.type.function;

import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZoneDateTimeTypeFunctionTest {

    @Test
    @DisplayName("Gets proper type")
    void gets_proper_type() {
        ZonedDateTimeTimeTypeFunction typeFunction = new ZonedDateTimeTimeTypeFunction(new SystemJsonObjectMapper(DescriptorFactory::new));
        assertTrue(typeFunction.canBuildType(ZonedDateTime.class, null));
        assertEquals("ZonedDateTime", typeFunction.getType().getName());
        Coercing<?, ?> coercing = typeFunction.getType().getCoercing();
        String dateString = "2022-08-16T14:17:54.282Z";
        ZonedDateTime date = (ZonedDateTime) coercing.parseLiteral(StringValue.of(dateString));
        assertEquals(ZonedDateTime.parse(dateString), date);
        assertEquals(date, coercing.serialize(date));

        Assertions.assertThrows(IllegalArgumentException.class, () -> coercing.parseLiteral(IntValue.of(1)));

    }
}