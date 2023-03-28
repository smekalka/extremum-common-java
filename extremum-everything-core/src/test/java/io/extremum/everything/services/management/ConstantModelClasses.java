package io.extremum.everything.services.management;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.support.ModelClasses;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * @author rpuch
 */
public class ConstantModelClasses implements ModelClasses {
    private final Map<String, Class<? extends Model>> modelNameToClassMap;

    public ConstantModelClasses(Map<String, Class<? extends Model>> modelNameToClassMap) {
        this.modelNameToClassMap = ImmutableMap.copyOf(modelNameToClassMap);
    }

    @Override
    public <M extends Model> Class<M> getClassByModelName(String modelName) {
        @SuppressWarnings("unchecked")
        Class<M> castResult = (Class<M>) modelNameToClassMap.get(modelName);
        return castResult;
    }

}
