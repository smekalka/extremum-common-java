package io.extremum.jpa.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.mapper.jackson.BasicJsonObjectMapper;

public abstract class BaseJSONConverter {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new BasicJsonObjectMapper();
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}