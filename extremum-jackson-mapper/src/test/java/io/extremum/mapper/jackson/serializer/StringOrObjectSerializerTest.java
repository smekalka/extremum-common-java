package io.extremum.mapper.jackson.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.mapper.jackson.BasicJsonObjectMapper;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.basic.StringOrObject;
import io.extremum.sharedmodels.personal.Name;
import io.extremum.sharedmodels.personal.PersonRequestDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class StringOrObjectSerializerTest {
    private static ObjectMapper mapper;

    @BeforeAll
    static void startup() {
        mapper = new BasicJsonObjectMapper();
    }

    static void assertEqualsJson(String expected, String result) throws IOException {
        ObjectMapper m = new ObjectMapper();
        assertEquals(m.readTree(expected), m.readTree(result));
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class TestObject {
        StringOrObject<Dummy> test;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Dummy {
        String a;
        String b;
    }

    @Test
    void objectSerializeTest() throws IOException {
        String res = mapper.writeValueAsString(new TestObject(new StringOrObject<>(new Dummy("a", "b"))));
        assertEqualsJson("{\"test\":{\"a\":\"a\",\"b\":\"b\"}}", res);
    }

    @Test
    void stringSerializeTest() throws IOException {
        String res = mapper.writeValueAsString(new TestObject(new StringOrObject<>("test")));
        assertEqualsJson("{\"test\":\"test\"}", res);
    }

    @Test
    void nullSerializeTest() throws IOException {
        String res = mapper.writeValueAsString(new TestObject(null));
        assertEqualsJson("{}", res);
    }

    @Test
    void objectDeserializeTest() throws IOException {
        TestObject res = mapper.readValue("{\"test\":{\"a\":\"a\",\"b\":\"b\"}}", TestObject.class);
        assertEquals(new TestObject(new StringOrObject<>(new Dummy("a", "b"))), res);
    }

    @Test
    void stringDeserializeTest() throws IOException {
        TestObject res = mapper.readValue("{\"test\":\"test\"}", TestObject.class);
        assertEquals(new TestObject(new StringOrObject<>("test")), res);
    }

    @Test
    void nullDeserializeTest() throws IOException {
        TestObject res = mapper.readValue("{\"test\":null}", TestObject.class);
        assertEquals(new TestObject(null), res);
    }

    @Test
    void complexObjectDeserializationTest() throws IOException {
        PersonRequestDto p = new PersonRequestDto();
        Name n = new Name();
        n.setFirst(new StringOrMultilingual("Ivan"));
        n.setLast(new StringOrMultilingual("Ivanov"));
        p.setName(new StringOrObject<>(n));
        String r = mapper.writeValueAsString(p);
        log.info("dto={}", r);
        PersonRequestDto pp = mapper.readValue(r, PersonRequestDto.class);
        assertEquals(p, pp);
    }
}
