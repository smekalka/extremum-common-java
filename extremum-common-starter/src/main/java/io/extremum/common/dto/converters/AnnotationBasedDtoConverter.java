package io.extremum.common.dto.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.annotation.View;
import io.extremum.common.annotation.ViewMode;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.dto.UniversalRequestDto;
import io.extremum.sharedmodels.dto.UniversalResponseDto;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AnnotationBasedDtoConverter<M extends BasicModel<?>>
        implements ToResponseDtoConverter<M, UniversalResponseDto>,
        ToRequestDtoConverter<M, UniversalRequestDto>,
        FromRequestDtoConverter<M, UniversalRequestDto> {

    private final ObjectMapper objectMapper;

    protected AnnotationBasedDtoConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public UniversalResponseDto convertToResponse(M model, ConversionConfig config) {
        UniversalResponseDto dto = new UniversalResponseDto(getFieldValueMap(model, config));
        dto.id = model.getUuid();

        return dto;
    }

    @Override
    public UniversalRequestDto convertToRequest(M model, ConversionConfig conversionConfig) {
        UniversalRequestDto dto = new UniversalRequestDto(getFieldValueMap(model, conversionConfig));
        dto.getData().put("id", model.getUuid());

        return dto;
    }


    @Override
    public M convertFromRequest(UniversalRequestDto universalRequestDto) {
        return objectMapper.convertValue(universalRequestDto.getData(), new TypeReference<M>() {
        });
    }

    @Override
    public Class<? extends UniversalRequestDto> getRequestDtoType() {
        return UniversalRequestDto.class;
    }

    @Override
    public Class<? extends UniversalResponseDto> getResponseDtoType() {
        return UniversalResponseDto.class;
    }

    private static Map<String, Object> getFieldValueMap(Object bean, ConversionConfig config) {
        Class<?> cls = bean.getClass();
        Map<String, Object> valueMap = new HashMap<>();
        Field[] fields = cls.getDeclaredFields();

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(bean);
                View annotation = field.getAnnotation(View.class);
                if (annotation != null && value != null) {
                    if (isCollection(field)) {
                        valueMap.put(field.getName(), ((Collection) value).stream()
                                .map(
                                        o -> getObjectAsKeyValue(config, field.getName(), o, annotation).values())
                                .collect(Collectors.toList()));
                    } else {
                        valueMap.putAll(getObjectAsKeyValue(config, field.getName(), value, annotation));
                    }
                } else {
                    valueMap.put(field.getName(), value);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return valueMap;
    }

    private static Map<String, Object> getObjectAsKeyValue(ConversionConfig config, String fieldName, Object value, View annotation) {
        Map<String, Object> objectFields = new HashMap<>();

        if (config.isExpand()) {
            if (annotation.expanded() == ViewMode.ID) {
                objectFields.put(fieldName, ((BasicModel<?>) value).getUuid().getExternalId());
            }
            if (annotation.expanded() == ViewMode.OBJECT) {
                objectFields.put(fieldName, getFieldValueMap(value, config));
            }
        } else {
            if (annotation.def() == ViewMode.ID) {
                objectFields.put(fieldName, ((BasicModel<?>) value).getUuid().getExternalId());
            }
            if (annotation.def() == ViewMode.OBJECT) {
                objectFields.put(fieldName, getFieldValueMap(value, config));
            }
        }

        return objectFields;
    }

    private static boolean isCollection(Field field) {
        return (Collection.class.isAssignableFrom(field.getType()));
    }
}
