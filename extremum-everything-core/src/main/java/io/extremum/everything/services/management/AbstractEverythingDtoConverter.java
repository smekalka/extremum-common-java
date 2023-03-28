package io.extremum.everything.services.management;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.FromRequestDtoConverter;
import io.extremum.common.dto.converters.ToRequestDtoConverter;
import io.extremum.common.dto.converters.ToResponseDtoConverter;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.exceptions.ConverterNotFoundException;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.model.PersistableCommonModel;
import io.extremum.common.urls.ApplicationUrls;
import io.extremum.everything.services.models.ModelSettingsProvider;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.basic.ModelSettings;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.Dto;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.UniversalRequestDto;
import io.extremum.sharedmodels.dto.UniversalResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractEverythingDtoConverter<M extends PersistableCommonModel<UUID>> implements
        ToResponseDtoConverter<M, UniversalResponseDto>,
        ToRequestDtoConverter<M, UniversalRequestDto>, FromRequestDtoConverter<M, UniversalRequestDto> {


    private final DtoConversionService dtoConversionService;
    private final ObjectMapper objectMapper;
    private final ModelRetriever modelRetriever;
    private final ApplicationUrls applicationUrls;
    private final DescriptorService descriptorService;
    private final ModelSettingsProvider modelSettingsProvider;

    protected AbstractEverythingDtoConverter(DtoConversionService dtoConversionService, ObjectMapper objectMapper, ModelRetriever modelRetriever, ApplicationUrls applicationUrls, DescriptorService descriptorService, ModelSettingsProvider modelSettingsProvider) {
        this.dtoConversionService = dtoConversionService;
        this.objectMapper = objectMapper;
        this.modelRetriever = modelRetriever;
        this.applicationUrls = applicationUrls;
        this.descriptorService = descriptorService;
        this.modelSettingsProvider = modelSettingsProvider;
    }

    public abstract Class<M> getSupportedModelClass();

    @Override
    @SneakyThrows
    public M convertFromRequest(UniversalRequestDto dto) {
        if (dto.getId() != null) {
            return (M) modelRetriever.retrieveModel(new Descriptor(dto.getId()));
        }
        Map<String, Object> data = dto.getData();
        M model = getSupportedModelClass().newInstance();
        PropertyAccessor propertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(model);
        for (Field field : getSupportedModelClass().getDeclaredFields()) {
            if (Collection.class.isAssignableFrom(field.getType())) {
                ParameterizedType listType = (ParameterizedType) field.getGenericType();
                Class<?> listItemClass = (Class<?>) listType.getActualTypeArguments()[0];
                List<Object> arrayList = objectMapper.convertValue(data.get(field.getName()), new TypeReference<List<Object>>() {
                });
                if (arrayList != null) {
                    List<Object> collect = arrayList.stream().map(item -> getSingularProperty(listItemClass, item)).collect(Collectors.toList());
                    propertyAccessor.setPropertyValue(field.getName(), collect);
                }
            } else {
                Object value = getSingularProperty(field.getType(), data.get(field.getName()));
                if (value != null) {
                    propertyAccessor.setPropertyValue(field.getName(), value);
                }
            }
        }

        Object uuid = dto.getData().get("uuid");
        if (uuid != null) {
            descriptorService.loadByExternalId((String) uuid).ifPresent(
                    descriptor -> {
                        model.setUuid(descriptor);
                        model.setId(UUID.fromString(descriptor.getInternalId()));
                    }
            );
        }

        model.setVersion((Long) dto.getData().get("version"));

        return model;
    }

    private Object getSingularProperty(Class<?> propertyType, Object propertyValue) {
        if (propertyValue != null) {
            try {
                UUID uuid = UUID.fromString(String.valueOf(propertyValue));
                Model model = modelRetriever.retrieveModel(new Descriptor(uuid.toString()));
                if (model.getClass().isAssignableFrom(propertyType)) {
                    return model;
                } else {
                    throw new ModelNotFoundException((Class<? extends Model>) propertyType, uuid.toString());
                }
            } catch (IllegalArgumentException illegalArgumentExceptionZ) {
                Class<? extends Model> declaringClass = (Class<? extends Model>) propertyType;
                try {
                    return invokeOwnedFieldConverter(propertyValue, declaringClass);
                } catch (RuntimeException exception) {
                    return objectMapper.convertValue(propertyValue, propertyType);
                }
            }
        } else {
            return null;
        }
    }

    @Override
    @SneakyThrows
    public UniversalResponseDto convertToResponse(M model, ConversionConfig config) {
        Function<Model, Dto> modelDtoFunction = (item) -> dtoConversionService.convertUnknownToResponseDto(item, config);

        Map<String, Object> data = new HashMap<>();
        ModelSettings settings = modelSettingsProvider.getSettings(model.getClass());

        if (!config.isExpand() && !config.getModel().equalsIgnoreCase(getSupportedModel())) {
            UniversalResponseDto dto = new UniversalResponseDto(data);
            dto.setId(model.getUuid());
            return dto;
        }
        for (Field field : getSupportedModelClass().getDeclaredFields()) {
            Method readMethod = new PropertyDescriptor(field.getName(), getSupportedModelClass()).getReadMethod();
            if (readMethod != null) {
                if (Collection.class.isAssignableFrom(field.getType())) {
                    try {
                        ParameterizedType listType = (ParameterizedType) field.getGenericType();
                        Class<?> listItemClass = (Class<?>) listType.getActualTypeArguments()[0];
                        if (Model.class.isAssignableFrom(listItemClass)) {
                            Collection<? extends Model> invoke = (Collection<? extends Model>) readMethod.invoke(model);
                            List<Dto> collect = invoke.stream().map(modelDtoFunction::apply).collect(Collectors.toList());
                            putFieldValueToMap(data, field.getName(), new CollectionReference<>(collect), settings, modelDtoFunction);
                        } else {
                            Object invoke = readMethod.invoke(model);
                            putFieldValueToMap(data, field.getName(), invoke, settings, modelDtoFunction);
                        }
                    } catch (ConverterNotFoundException exception) {
                        Collection<?> invoke = (Collection<?>) readMethod.invoke(model);
                        putFieldValueToMap(data, field.getName(), invoke, settings, modelDtoFunction);
                    }
                } else {
                    try {
                        Object invoke = readMethod.invoke(model);
                        if (invoke instanceof Model) {
                            putFieldValueToMap(data, field.getName(), dtoConversionService.convertUnknownToResponseDto((Model) invoke, config), settings, modelDtoFunction);
                        } else {
                            putFieldValueToMap(data, field.getName(), readMethod.invoke(model), settings, modelDtoFunction);
                        }
                    } catch (ConverterNotFoundException exception) {
                        putFieldValueToMap(data, field.getName(), readMethod.invoke(model), settings, modelDtoFunction);
                    }
                }
            }
        }

        UniversalResponseDto dto = new UniversalResponseDto(data);
        dto.setId(model.getUuid());
        dto.setIri(applicationUrls.createExternalUrl(model.getUuid().getExternalId()));
        dto.setVersion(model.getVersion());
        dto.setCreated(model.getCreated());
        dto.setModified(model.getModified());

        return dto;
    }

    private void putFieldValueToMap(Map<String, Object> data, String fieldName, Object object, ModelSettings settings, Function<Model, Dto> modelDtoFunction) {
        if (settings != null) {
            if (settings.getProperties().getVisible().contains(fieldName)) {

                Object fieldValue = getFieldValue(object, settings.getProperties().getVisible().stream().filter(s -> s.startsWith(fieldName + ".")).map(s -> StringUtils.substringAfter(s, ".")).collect(Collectors.toList()), modelDtoFunction);
                data.put(fieldName, fieldValue);
            }
        } else {
            data.put(fieldName, object);
        }
    }

    private Object getFieldValue(Object object, List<String> visible, Function<Model, Dto> modelDtoFunction) {
        if (object == null || object instanceof CollectionReference) {
            return object;
        }
        if (visible.isEmpty()) {
            if (Model.class.isAssignableFrom(object.getClass())) {
                return modelDtoFunction.apply((Model) object);
            } else {
                return object;
            }
        }
        try {
            if (Collection.class.isAssignableFrom(object.getClass())) {
                List<Object> result = new ArrayList<>();
                ((Collection<?>) object).forEach(
                        item -> {
                            if (Model.class.isAssignableFrom(item.getClass())) {
                                result.add(modelDtoFunction.apply((Model) item));
                            } else {
                                Map<String, Object> values = new HashMap<>();
                                for (Field declaredField : item.getClass().getDeclaredFields()) {
                                    putToValues(item, visible, modelDtoFunction, values, declaredField);
                                }

                                result.add(values);
                            }
                        }
                );
                return result;
            } else {
                Field[] declaredFields = object.getClass().getDeclaredFields();
                Map<String, Object> values = new HashMap<>();

                for (Field declaredField : declaredFields) {
                    declaredField.setAccessible(true);
                    putToValues(object, visible, modelDtoFunction, values, declaredField);
                }

                return values;

            }
        } catch (Exception e) {
            log.warn("Object {} converted with errors", object);
            return object;
        }
    }

    private void putToValues(Object object, List<String> visible, Function<Model, Dto> modelDtoFunction, Map<String, Object> values, Field declaredField) {
        if (visible.contains(declaredField.getName())) {
            declaredField.setAccessible(true);
            try {
                values.put(declaredField.getName(), getFieldValue(declaredField.get(object), visible.stream().filter(s -> s.startsWith(declaredField.getName() + ".")).map(s -> StringUtils.substringAfter(s, ".")).collect(Collectors.toList()), modelDtoFunction));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    @SneakyThrows
    public UniversalRequestDto convertToRequest(M model, ConversionConfig config) {
        Function<Model, Dto> modelDtoFunction = (item) -> dtoConversionService.convertUnknownToRequestDto(item, config);

        Map<String, Object> data = new HashMap<>();
        ModelSettings settings = modelSettingsProvider.getSettings(model.getClass());

        for (Field field : getSupportedModelClass().getDeclaredFields()) {
            Method readMethod = new PropertyDescriptor(field.getName(), getSupportedModelClass()).getReadMethod();
            if (readMethod != null) {
                if (Collection.class.isAssignableFrom(field.getType())) {
                    try {
                        ParameterizedType listType = (ParameterizedType) field.getGenericType();
                        Class<?> listItemClass = (Class<?>) listType.getActualTypeArguments()[0];
                        if (Model.class.isAssignableFrom(listItemClass)) {
                            Collection<? extends Model> invoke = (Collection<? extends Model>) readMethod.invoke(model);
                            List<RequestDto> collect = invoke.stream().map(item -> dtoConversionService.convertUnknownToRequestDto(item, config)).collect(Collectors.toList());
                            putFieldValueToMap(data, field.getName(), collect, settings, modelDtoFunction);
                        } else {
                            Object invoke = readMethod.invoke(model);
                            putFieldValueToMap(data, field.getName(), invoke, settings, modelDtoFunction);
                        }
                    } catch (ConverterNotFoundException exception) {
                        Collection<?> invoke = (Collection<?>) readMethod.invoke(model);
                        putFieldValueToMap(data, field.getName(), invoke, settings, modelDtoFunction);
                    }
                } else {
                    try {
                        Object invoke = readMethod.invoke(model);
                        if (invoke instanceof Model) {
                            putFieldValueToMap(data, field.getName(), dtoConversionService.convertUnknownToRequestDto((Model) invoke, config), settings, modelDtoFunction);
                        } else {
                            putFieldValueToMap(data, field.getName(), readMethod.invoke(model), settings, modelDtoFunction);
                        }
                    } catch (ConverterNotFoundException exception) {
                        putFieldValueToMap(data, field.getName(), readMethod.invoke(model), settings, modelDtoFunction);
                    }
                }
            }
        }


        data.put("uuid", model.getUuid().getExternalId());
        data.put("created", model.getCreated());
        data.put("version", model.getVersion());
        data.put("modified", model.getModified());

        return new UniversalRequestDto(data);
    }

    private Model invokeOwnedFieldConverter(Object propertyValue, Class<? extends Model> declaringClass) {
        return dtoConversionService.convertFromRequestDto(
                declaringClass,
                new UniversalRequestDto(objectMapper.convertValue(propertyValue, LinkedHashMap.class))
        );
    }

    public Class<? extends UniversalRequestDto> getRequestDtoType() {
        return UniversalRequestDto.class;
    }

    public Class<? extends UniversalResponseDto> getResponseDtoType() {
        return UniversalResponseDto.class;
    }

}