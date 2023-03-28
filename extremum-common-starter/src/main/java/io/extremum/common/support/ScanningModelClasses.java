package io.extremum.common.support;

import com.google.common.collect.ImmutableMap;
import io.extremum.common.annotation.ModelName;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.utils.FindUtils;
import io.extremum.common.utils.ModelUtils;
import io.extremum.sharedmodels.basic.Model;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class ScanningModelClasses implements ModelClasses {
    private final Map<String, Class<? extends Model>> modelNameToClassMap;

    public ScanningModelClasses(List<String> modelPackages) {
        List<Class<? extends Model>> models = new ArrayList<>();
        for (String packageName : modelPackages) {
            models.addAll(FindUtils.findClassesByAnnotation(Model.class, ModelName.class, packageName));
        }

        Map<String, Class<? extends Model>> modelClasses = models.stream()
                .collect(Collectors.toMap(
                        modelClass -> ModelUtils.getModelName(modelClass).toLowerCase(),
                        aClass -> aClass,
                        (aClass, aClass2) -> {
                            throw new IllegalStateException(
                                    "Found 2 model classes with same model name: " + aClass + " and " + aClass2);
                        }));
        modelNameToClassMap = ImmutableMap.copyOf(modelClasses);
    }

    @Override
    public <M extends Model> Class<M> getClassByModelName(String modelName) {
        @SuppressWarnings("unchecked")
        Class<M> castResult = (Class<M>) Optional.ofNullable(modelNameToClassMap.get(modelName.toLowerCase()))
                .orElseThrow(() -> {
                    log.error("Model with name " + modelName
                            + " is not known, probably it doesn't have @ModelName annotation.");
                    return new ModelNotFoundException("Model with name " + modelName
                            + " is not known.");
                });

        return castResult;
    }

}
