package io.extremum.mapper.jackson.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.mapper.jackson.BasicJsonObjectMapper;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringOrMultilingualSerializerTest {
    private ObjectMapper mapper = new BasicJsonObjectMapper();

    @Test
    public void serializeToSimpleTextTest() throws JsonProcessingException {
        StringOrMultilingual ml = new StringOrMultilingual("В лесу родилась ёлочка");
        String json = mapper.writeValueAsString(ml);
        assertNotNull(json);
        assertEquals("\"В лесу родилась ёлочка\"", json);
    }

    @Test
    public void serializeToComplexObjectTest() throws Exception {
        String expectedText_ru = "В лесу родилась ёлочка";
        String expectedLang_ru = "ru-RU";
        String expectedText_en = "The forest raised a christmas tree";
        String expectedLang_en = "en-US";

        Map<MultilingualLanguage, String> map = new HashMap<>();
        map.put(MultilingualLanguage.ru_RU, expectedText_ru);
        map.put(MultilingualLanguage.en_US, expectedText_en);

        StringOrMultilingual ml = new StringOrMultilingual(map);
        String json = mapper.writeValueAsString(ml);

        JSONObject jsonObject = new JSONObject(json);
        assertTrue(jsonObject.has(expectedLang_ru));
        assertTrue(jsonObject.has(expectedLang_en));
        assertEquals(expectedText_ru, jsonObject.getString(expectedLang_ru));
        assertEquals(expectedText_en, jsonObject.getString(expectedLang_en));
    }

    @Test
    public void serializeNullTest() throws JsonProcessingException {
        StringOrMultilingual ml = new StringOrMultilingual();
        String json = mapper.writeValueAsString(ml);
        assertEquals("null", json);
    }
}