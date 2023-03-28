package io.extremum.mapper.jackson.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.mapper.jackson.BasicJsonObjectMapper;
import io.extremum.sharedmodels.structs.DurationVariativeValue;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DurationVariativeValueSerializerTest {
    private ObjectMapper mapper = new BasicJsonObjectMapper();

    @Test
    public void simpleIntSerializeTest() throws JsonProcessingException {
        DurationVariativeValue duration = new DurationVariativeValue(1);
        String jsonString = mapper.writeValueAsString(duration);
        assertEquals("1", jsonString);
    }

    @Test
    public void simpleStringSerializeTest() throws JsonProcessingException {
        DurationVariativeValue duration = new DurationVariativeValue("1h");
        String jsonString = mapper.writeValueAsString(duration);
        assertEquals("\"1h\"", jsonString);
    }

    @Test
    public void objectIntSerializeTest() throws Exception {
        DurationVariativeValue duration = new DurationVariativeValue(1, 2);
        String jsonString = mapper.writeValueAsString(duration);
        JSONObject json = new JSONObject(jsonString);

        assertTrue(json.has(DurationVariativeValue.FIELDS.min.name()));
        assertTrue(json.has(DurationVariativeValue.FIELDS.max.name()));

        assertEquals(1, json.getInt(DurationVariativeValue.FIELDS.min.name()));
        assertEquals(2, json.getInt(DurationVariativeValue.FIELDS.max.name()));
    }

    @Test
    public void objectStringSerializeTest() throws Exception {
        DurationVariativeValue duration = new DurationVariativeValue("1h 30m", "2h");
        String jsonString = mapper.writeValueAsString(duration);
        JSONObject json = new JSONObject(jsonString);

        assertTrue(json.has(DurationVariativeValue.FIELDS.min.name()));
        assertTrue(json.has(DurationVariativeValue.FIELDS.max.name()));

        assertEquals("1h 30m", json.getString(DurationVariativeValue.FIELDS.min.name()));
        assertEquals("2h", json.getString(DurationVariativeValue.FIELDS.max.name()));
    }

    @Test
    public void objectMixedSerializeTest() throws Exception {
        DurationVariativeValue duration = new DurationVariativeValue("1h 30m", 2);
        String jsonString = mapper.writeValueAsString(duration);
        JSONObject json = new JSONObject(jsonString);

        assertTrue(json.has(DurationVariativeValue.FIELDS.min.name()));
        assertTrue(json.has(DurationVariativeValue.FIELDS.max.name()));

        assertEquals("1h 30m", json.getString(DurationVariativeValue.FIELDS.min.name()));
        assertEquals(2, json.getInt(DurationVariativeValue.FIELDS.max.name()));
    }
}