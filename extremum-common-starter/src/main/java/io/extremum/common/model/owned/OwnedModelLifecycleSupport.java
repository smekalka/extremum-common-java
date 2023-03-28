package io.extremum.common.model.owned;

import io.extremum.common.model.owned.model.OwnedModel;
import io.extremum.sharedmodels.basic.Model;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OwnedModelLifecycleSupport {

    public List<Field> getOwnedFields(Model folder) {
        return Arrays.stream(FieldUtils.getAllFields(folder.getClass())).filter(field -> getAllFieldInterfaces(field).contains(OwnedModel.class)).collect(Collectors.toList());
    }

    private List<Class<?>> getAllFieldInterfaces(Field field) {
        return ClassUtils.getAllSuperclasses(field.getType());
    }
}