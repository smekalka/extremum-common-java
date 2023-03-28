package io.extremum.schema.service;

import io.extremum.common.annotation.Inherited;
import io.extremum.common.annotation.Schema;
import io.extremum.common.utils.FindUtils;
import io.extremum.everything.services.models.ModelSettingsProvider;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.basic.ModelSettings;
import io.extremum.sharedmodels.schema.RegisteredSchema;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ModelSchemaRegistrar {

    @Getter
    private LinkedHashSet<RegisteredSchema> modelSchemas;

    private Collection<String> getProperties(Field field, String root, Collection<String> result) {
        for (Field declaredField : getMainType(field).getDeclaredFields()) {
            result.add(root + declaredField.getName());
            if (Arrays.stream(field.getType().getClasses()).anyMatch(aClass -> !getMainType(declaredField).isEnum() &&
                    aClass.equals(getMainType(declaredField)))) {
                getProperties(declaredField, root + declaredField.getName() + ".", result);
            }
        }

        return result;
    }

    private Collection<String> getProperties(Class<?> clazz, Collection<String> result) {
        for (Field declaredField : clazz.getDeclaredFields()) {
            result.add(declaredField.getName());
            if (Arrays.stream(clazz.getClasses()).anyMatch(aClass -> !getMainType(declaredField).isEnum() &&
                    aClass.equals(getMainType(declaredField)))) {
                getProperties(declaredField, declaredField.getName() + ".", result);
            }
        }

        return result;
    }

    private Class<?> getMainType(Field field) {
        if (Collection.class.isAssignableFrom(field.getType())) {
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            return (Class<?>) genericType.getActualTypeArguments()[0];
        } else {
            return field.getType();
        }
    }


    public ModelSchemaRegistrar(List<String> modelPackages, ModelSettingsProvider modelSettingsProvider) {
        List<Class<? extends Model>> models = new ArrayList<>();
        for (String packageName : modelPackages) {
            models.addAll(FindUtils.findClassesByAnnotation(Model.class, Schema.class, packageName));
        }

        Map<RegisteredSchema, ModelSettings> settingsMap = new HashMap<>();
        modelSchemas = models
                .stream()
                .map(
                        clazz -> {
                            RegisteredSchema registeredSchema = new RegisteredSchema(clazz.getAnnotation(Schema.class).id());
                            Set<String> result = new HashSet<>();
                            getProperties(clazz, result);
                            settingsMap.put(
                                    registeredSchema,
                                    new ModelSettings(new ModelSettings.Properties(result), clazz)
                            );
                            return registeredSchema;
                        }
                )
                .sorted(Comparator.comparing(RegisteredSchema::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        models
                .forEach(
                        aClass -> Arrays.stream(aClass.getDeclaredFields()).filter(
                                        field -> field.getAnnotation(Inherited.class) != null
                                )
                                .forEach(
                                        field -> {
                                            Inherited inheritedAnnotation = field.getAnnotation(Inherited.class);
                                            String inheritedFrom = inheritedAnnotation.schema();
                                            Optional<RegisteredSchema> parentSchemaOpt = modelSchemas.stream().filter(registeredSchema -> registeredSchema.getId().equals(inheritedFrom)).findFirst();
                                            if (parentSchemaOpt.isPresent()) {
                                                Schema schemaAnnotation = aClass.getAnnotation(Schema.class);
                                                Optional<RegisteredSchema> inheritorOpt = modelSchemas.stream().filter(registeredSchema -> registeredSchema.getId().equals(schemaAnnotation.id())).findFirst();
                                                inheritorOpt.ifPresent(inheritor -> parentSchemaOpt.get().addInheritor(inheritor));
                                            } else {
                                                RegisteredSchema parentSchema = new RegisteredSchema(inheritedFrom);
                                                modelSchemas.add(parentSchema);
                                                Schema schemaAnnotation = aClass.getAnnotation(Schema.class);
                                                Optional<RegisteredSchema> inheritorOpt = modelSchemas.stream().filter(registeredSchema -> registeredSchema.getId().equals(schemaAnnotation.id())).findFirst();
                                                inheritorOpt.ifPresent(parentSchema::addInheritor);
                                                settingsMap.put(parentSchema, new ModelSettings(
                                                        new ModelSettings.Properties(
                                                                Arrays.stream(aClass.getDeclaredFields()).filter(f -> isAnnotatedWithInherited(f, parentSchema.getId())).map(Field::getName).collect(Collectors.toSet())
                                                        ),
                                                        null
                                                ));
                                            }
                                        }
                                )
                );

        modelSchemas = new LinkedHashSet<>(modelSchemas);
        modelSettingsProvider.loadSettings(settingsMap);
    }

    private boolean isAnnotatedWithInherited(Field field, String id) {
        Inherited annotation = field.getAnnotation(Inherited.class);
        if (annotation == null) {
            return false;
        }

        return annotation.schema().equals(id);
    }
}
