package io.extremum.dynamic.services.impl;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.extremum.datetime.ApiDateTimeFormat;
import io.extremum.dynamic.services.DateTypesNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class DefaultDateTypesNormalizer implements DateTypesNormalizer {
    private final ApiDateTimeFormat apiDateTimeFormat = new ApiDateTimeFormat();

    @Override
    public Map<String, Object> normalize(Map<String, Object> doc, Collection<String> datePaths) {
        for (String path : datePaths) {
            try {
                Object value = JsonPath.read(doc, path);
                if (value instanceof String) {
                    ZonedDateTime zdt = toZonedDateTime((String) value);
                    JsonPath.compile(path)
                            .set(doc, Date.from(zdt.toInstant()), Configuration.builder().build());
                }
            } catch (PathNotFoundException e) {
                log.warn("Path {} wasn't found in doc {}", path, doc);
            }
        }

        return doc;
    }

    private ZonedDateTime toZonedDateTime(String value) {
        return apiDateTimeFormat.parse(value);
    }
}
