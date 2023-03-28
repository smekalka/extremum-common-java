package io.extremum.descriptors.common;

import io.extremum.common.annotation.Stump;
import io.extremum.sharedmodels.basic.BasicModel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class AnnotationBasedStumpFacilities implements StumpFacilities {

    private List<String> defaultFields = new ArrayList<>();

    @Override
    public Map<String, Object> getStump(Object object) {
        Stump annotation = object.getClass().getAnnotation(Stump.class);
        List<String> fieldProvidedByAnnotation = new ArrayList<>();
        if (annotation != null) {
            fieldProvidedByAnnotation = Arrays.asList(annotation.fields());
        }

        HashMap<String, Object> stump = new HashMap<>();

        Stream.concat(
                fieldProvidedByAnnotation.stream(),
                defaultFields.stream()
        ).forEach(
                s -> {
                    try {
                        Field declaredField = object.getClass().getDeclaredField(s);
                        try {
                            declaredField.setAccessible(true);
                            Object value = declaredField.get(object);

                            if (value != null && !BasicModel.class.isAssignableFrom(value.getClass())) {
                                stump.put(declaredField.getName(), value);
                            } else {
                                if (value != null) {
                                    log.warn("Only non BasicModel fields allowed, but field class is {}", value.getClass());
                                }
                            }
                        } catch (IllegalAccessException e) {
                            log.error("Unable to access field {} to create stump", s, e);
                        }

                    } catch (NoSuchFieldException e) {
                        log.warn("Field {} ignored, because field with name not present {}", s, s);
                    }
                }
        );

        return stump;
    }

    protected void setDefaultFields(List<String> fields) {
        this.defaultFields = fields;
    }
}
