package io.extremum.mapper.jackson.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.mapper.jackson.BasicJsonObjectMapper;
import io.extremum.mapper.util.TestUtils;
import io.extremum.sharedmodels.structs.DurationVariativeValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class DurationVariativeValueDeserializerTest {
    private ObjectMapper mapper = new BasicJsonObjectMapper();

    @Test
    public void simpleIntDeserializeTest() throws IOException {
        String jsonString = loadJson("DurationVariativeValue-simple-int.json");
        DurationVariativeValue duration = mapper.readValue(jsonString, DurationVariativeValue.class);

        assertNotNull(duration);
        assertTrue(duration.isContainsCommonValueInteger());
        assertEquals(1, (int) duration.commonIntValue);
    }

    @Test
    public void simpleStringDeserializeTest() throws IOException {
        String jsonString = loadJson("DurationVariativeValue-simple-string.json");
        DurationVariativeValue duration = mapper.readValue(jsonString, DurationVariativeValue.class);

        assertNotNull(duration);
        assertTrue(duration.isContainsCommonValueString());
        assertEquals("1h 30m", duration.commonStringValue);
    }

    @Test
    public void objectIntDeserializeTest() throws IOException {
        String jsonString = loadJson("DurationVariativeValue-object-int.json");
        DurationVariativeValue duration = mapper.readValue(jsonString, DurationVariativeValue.class);

        assertNotNull(duration);
        assertTrue(duration.isContainsObject());
        assertTrue(duration.isContainsMaxValueInteger());
        assertTrue(duration.isContainsMinValueInteger());
        assertEquals(1, (int) duration.minIntValue);
        assertEquals(1, (int) duration.maxIntValue);
    }

    @Test
    public void objectStringDeserializeTest() throws IOException {
        String jsonString = loadJson("DurationVariativeValue-object-string.json");
        DurationVariativeValue duration = mapper.readValue(jsonString, DurationVariativeValue.class);

        assertNotNull(duration);
        assertTrue(duration.isContainsObject());
        assertTrue(duration.isContainsMaxValueString());
        assertTrue(duration.isContainsMinValueString());
        assertEquals("\"1h\"", duration.minStringValue);
        assertEquals("\"1m\"", duration.maxStringValue);
    }

    @Test
    public void objectMixedDeserializeTest() throws IOException {
        String jsonString = loadJson("DurationVariativeValue-object-mixed.json");
        DurationVariativeValue duration = mapper.readValue(jsonString, DurationVariativeValue.class);

        assertNotNull(duration);
        assertTrue(duration.isContainsObject());
        assertTrue(duration.isContainsMaxValueInteger());
        assertTrue(duration.isContainsMinValueString());
        assertEquals("\"1h\"", duration.minStringValue);
        assertEquals(1, (int) duration.maxIntValue);
    }

    private String loadJson(String path) {
        return TestUtils.loadAsStringFromResource("json-files/" + path);
    }
}