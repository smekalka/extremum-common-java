package io.extremum.jpa.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;

public class ObjectConverter extends BaseJSONConverter implements AttributeConverter<Object, String> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ObjectConverter.class);

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        final ObjectMapper mapper = getMapper();
        if (attribute == null) {
            return "";
        }
        try {
            return attribute.getClass().getName() + "|" + mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            LOGGER.error("Exception while converting to database column", e);
            return null;
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        final ObjectMapper mapper = getMapper();
        try {
            if (StringUtils.isBlank(dbData)) {
                return null;
            }
            final String[] parts = dbData.split("\\|", 2);
            return mapper.readValue(parts[1], Class.forName(parts[0]));
        } catch (Exception e) {
            LOGGER.error("Exception while converting to entity attribute", e);
            return null;
        }
    }
}