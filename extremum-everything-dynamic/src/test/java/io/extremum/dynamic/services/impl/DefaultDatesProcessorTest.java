package io.extremum.dynamic.services.impl;

import org.bson.Document;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultDatesProcessorTest {
    @Test
    void replaceDateWithZonedDateTime_firstLevel() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("strField", "simpleString");
        map.put("dateField", new Date());

        Document document = new Document(map);

        DefaultDatesProcessor processor = new DefaultDatesProcessor();
        Map<String, Object> processed = processor.processDates(document);

        // assert dates

        assertThat(processed.get("dateField"), instanceOf(ZonedDateTime.class));

        // assert others
        assertEquals("simpleString", processed.get("strField"));
    }

    @Test
    void replaceDateWithZonedDateTime_secondLevelInMap() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("strField", "simpleString");
        map.put("objField",
                createMap("dateField", new Date()));

        Document document = new Document(map);

        DefaultDatesProcessor processor = new DefaultDatesProcessor();
        Map<String, Object> processed = processor.processDates(document);

        // assert dates

        assertThat(of(processed.get("objField")).map(Map.class::cast).get()
                .get("dateField"), instanceOf(ZonedDateTime.class));

        // assert others
        assertEquals("simpleString", processed.get("strField"));
    }

    @Test
    void replaceDateWithZonedDateTime_fieldsInNestedList() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("strField", "simpleString");

        List<Object> list = new ArrayList<>();
        list.add(createMap("objField",
                createMap("dateField", new Date())));
        list.add(createMap("strField", "simpleString"));
        list.add(createMap("objField2",
                createMap("dateField", new Date())));
        list.add(createMap("mapInListField", createMap("intValue", 1)));
        list.add(new Date());

        map.put("arrayField", list);

        Set<Object> set = new HashSet<>();
        set.add("stringItem");
        set.add(new Date());
        set.add(1);

        list.add(createMap("setField", set));

        Document document = new Document(map);

        DefaultDatesProcessor processor = new DefaultDatesProcessor();
        Map<String, Object> processed = processor.processDates(document);

        // assert dates

        assertThat(of(processed.get("arrayField")).map(List.class::cast)
                        .map(m -> m.get(0))
                        .map(Map.class::cast)
                        .map(m -> m.get("objField"))
                        .map(Map.class::cast)
                        .map(m -> m.get("dateField")).orElse(null),
                instanceOf(ZonedDateTime.class));

        assertThat(of(processed.get("arrayField")).map(List.class::cast)
                        .map(l -> l.get(2))
                        .map(Map.class::cast)
                        .map(m -> m.get("objField2"))
                        .map(Map.class::cast)
                        .map(m -> m.get("dateField")).orElse(null),
                instanceOf(ZonedDateTime.class));

        assertThat(of(processed.get("arrayField")).map(List.class::cast)
                        .map(l -> l.get(4))
                        .orElse(null),
                instanceOf(ZonedDateTime.class));

        assertTrue(of(processed.get("arrayField")).map(List.class::cast)
                .map(l -> l.get(5))
                .map(Map.class::cast)
                .map(m -> m.get("setField"))
                .map(Set.class::cast)
                .filter(s -> s.stream().anyMatch(i -> i instanceof ZonedDateTime))
                .isPresent());


        // assert others
        assertEquals("simpleString", processed.get("strField"));
        assertThat("simpleString", CoreMatchers.is(
                of(processed.get("arrayField"))
                        .map(List.class::cast)
                        .map(l -> l.get(1))
                        .map(Map.class::cast)
                        .map(m -> m.get("strField"))
                        .map(String.class::cast).orElse(null)
        ));
        assertThat(1, CoreMatchers.is(
                of(processed.get("arrayField"))
                        .map(List.class::cast)
                        .map(l -> l.get(3))
                        .map(Map.class::cast)
                        .map(m -> m.get("mapInListField"))
                        .map(Map.class::cast)
                        .map(m -> m.get("intValue"))
                        .map(Integer.class::cast).orElse(null)
        ));
    }

    private Map<String, Object> createMap(String key, Object value) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }
}