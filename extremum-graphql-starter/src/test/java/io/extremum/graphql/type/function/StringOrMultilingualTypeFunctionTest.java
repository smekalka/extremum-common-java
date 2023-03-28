package io.extremum.graphql.type.function;

import graphql.language.IntValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringOrMultilingualTypeFunctionTest {

    @Test
    @DisplayName("Gets proper type")
    void gets_proper_type() {
        StringOrMultilingualTypeFunction typeFunction = new StringOrMultilingualTypeFunction(Locale.forLanguageTag("en-GB"), new SystemJsonObjectMapper(DescriptorFactory::new));
        assertTrue(typeFunction.canBuildType(StringOrMultilingual.class, null));
        assertEquals("StringOrMultilingual", typeFunction.getType().getName());
        Coercing<?, ?> coercing = typeFunction.getType().getCoercing();
        StringOrMultilingual value = (StringOrMultilingual) coercing.parseLiteral(StringValue.of("Extreмум"));
        assertEquals(new StringOrMultilingual("Extreмум"), value);

        value = (StringOrMultilingual) coercing.parseLiteral(
                ObjectValue.newObjectValue()
                        .objectField(
                                ObjectField
                                        .newObjectField()
                                        .name("en_GB")
                                        .value(StringValue.of("Extremum"))
                                        .build()
                        )
                        .objectField(
                                ObjectField
                                        .newObjectField()
                                        .name("ru_RU")
                                        .value(StringValue.of("Экстремум"))
                                        .build()
                        )
                        .build()
        );
        assertEquals(new StringOrMultilingual(new HashMap<MultilingualLanguage, String>() {{
            put(MultilingualLanguage.en_GB, "Extremum");
            put(MultilingualLanguage.ru_RU, "Экстремум");
        }}, Locale.forLanguageTag("en-GB")), value);

        Assertions.assertThrows(IllegalArgumentException.class, () -> coercing.parseLiteral(IntValue.of(1)));
    }
}