package io.extremum.dynamic.services.impl;

import io.extremum.dynamic.services.DatesProcessor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class DefaultDatesProcessor implements DatesProcessor {
    @Override
    public Map<String, Object> processDates(Map<String, Object> modelData) {
        Set<Map.Entry<String, Object>> entries = modelData.entrySet();

        replaceInEntries(entries);

        return modelData;
    }

    private void replaceInEntries(Set<Map.Entry<String, Object>> entries) {
        for (Map.Entry<String, Object> entry : entries) {
            replaceInCollection(entry);
        }
    }

    @SuppressWarnings("unchecked")
    private void replaceInCollection(Map.Entry<String, Object> entry) {
        Object value = entry.getValue();

        if (value instanceof Date) {
            entry.setValue(convertDateToString((Date) value));
        } else if (value instanceof Map) {
            replaceInEntries(((Map<String, Object>) value).entrySet());
        } else if (value instanceof List) {
            replaceInList((List<Object>) value);
        } else if (value instanceof Set) {
            Set<Object> set = replaceInSet((Set<Object>) value);
            entry.setValue(set);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Object> replaceInSet(Set<Object> set) {
        Set<Object> localSet = new HashSet<>();

        for (Object item : set) {
            if (item instanceof Date) {
                localSet.add(convertDateToString((Date) item));
            } else if (item instanceof Map) {
                replaceInEntries(((Map<String, Object>) item).entrySet());
                localSet.add(item);
            } else if (item instanceof List) {
                replaceInList((List<Object>) item);
                localSet.add(item);
            } else {
                localSet.add(item);
            }
        }

        return localSet;
    }

    @SuppressWarnings("unchecked")
    private void replaceInList(List<Object> list) {
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (item instanceof Map) {
                replaceInEntries(((Map<String, Object>) item).entrySet());
            } else if (item instanceof Date) {
                list.set(i, convertDateToString((Date) item));
            } else if (item instanceof Set) {
                replaceInSet((Set<Object>) item);
            }
        }
    }

    private ZonedDateTime convertDateToString(Date value) {
        return ZonedDateTime.ofInstant(value.toInstant(), ZoneId.of("UTC"));
    }
}
