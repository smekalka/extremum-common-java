package io.extremum.common.utils;

import io.extremum.common.model.annotation.HardDelete;
import io.extremum.common.annotation.ModelName;
import io.extremum.sharedmodels.basic.Model;

public final class ModelUtils {
    public static String getModelName(Class<?> modelClass) {
        ModelName annotation = findModelNameAnnotation(modelClass);
        if (annotation == null) {
            throw new IllegalStateException(modelClass + " is not annotated with @ModelName");
        }
        return annotation.value();
    }

    private static ModelName findModelNameAnnotation(Class<?> modelClass) {
        return AnnotationUtils.findAnnotationDirectlyOrUnderProxy(ModelName.class, modelClass);
    }

    public static boolean hasModelName(Class<? extends Model> modelClass) {
        return findModelNameAnnotation(modelClass) != null;
    }

    public static String getModelName(Object model) {
        return getModelName(model.getClass());
    }

    public static boolean isSoftDeletable(Class<?> modelClass) {
        HardDelete hardDelete = AnnotationUtils.findAnnotationDirectlyOrUnderProxy(HardDelete.class, modelClass);
        return hardDelete == null;
    }

    private ModelUtils() {
    }
}
