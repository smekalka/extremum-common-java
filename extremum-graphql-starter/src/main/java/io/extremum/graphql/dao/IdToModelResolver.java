package io.extremum.graphql.dao;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.Model;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

import java.lang.reflect.Field;

@AllArgsConstructor
public class IdToModelResolver {

    private final ModelRetriever modelRetriever;

    @SneakyThrows
    public <T extends BasicModel<?>> void resolveNestedModels(T model) {
        Field[] declaredFields = model.getClass().getDeclaredFields();
        PropertyAccessor propertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(model);
        for (Field declaredField : declaredFields) {
            if (BasicModel.class.isAssignableFrom(declaredField.getType())) {
                if (!declaredField.isAccessible()) {
                    declaredField.setAccessible(true);
                }
                BasicModel<?> o = (BasicModel<?>) declaredField.get(model);
                if (o != null)
                    if (o.getUuid() != null) {
                        Model nested = modelRetriever.retrieveModel(o.getUuid());
                        if (!declaredField.getType().isAssignableFrom(nested.getClass())) {
                            throw new ModelNotFoundException((Class<? extends Model>) declaredField.getType(), o.getUuid().toString());
                        }
                        propertyAccessor.setPropertyValue(declaredField.getName(), nested);
                    } else {
                        resolveNestedModels(o);
                    }
            }
        }
    }
}
