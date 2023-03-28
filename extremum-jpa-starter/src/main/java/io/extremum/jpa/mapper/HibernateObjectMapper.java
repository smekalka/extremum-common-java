package io.extremum.jpa.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.mapper.jackson.BasicJsonObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Supplier;

public class HibernateObjectMapper implements Supplier<ObjectMapper> {

    private static ObjectMapper objectMapper = new BasicJsonObjectMapper();

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        HibernateObjectMapper.objectMapper = objectMapper;
    }

    @Override
    public ObjectMapper get() {
        return objectMapper;
    }
}