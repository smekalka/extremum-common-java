package io.extremum.schema.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.everything.services.models.ModelSettingsProvider;
import io.extremum.sharedmodels.basic.ModelSettings;
import io.extremum.sharedmodels.schema.RegisteredSchema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class FileSystemModelSettingsProvider implements ModelSettingsProvider {

    private final File storage = new File("model.settings");
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private final MessageChannel modelSettingMessageChannel;
    private Map<String, ModelSettings> modelSettingsMap = new HashMap<>();
    private Set<RegisteredSchema> schemas = new HashSet<>();

    @SneakyThrows
    public FileSystemModelSettingsProvider(MessageChannel modelSettingMessageChannel) {
        this.modelSettingMessageChannel = modelSettingMessageChannel;
        if (!storage.exists()) {
            storage.createNewFile();
            mapper.writeValue(storage, new HashMap<String, ModelSettings>());
        } else {
            modelSettingsMap = readSettings();
        }
    }

    public ModelSettings getSettings(String fullName) {
        Map<String, ModelSettings> modelSettingsMap = readSettings();
        return modelSettingsMap
                .keySet()
                .stream()
                .filter(modelSchemaId -> modelSchemaId.equals(fullName))
                .findFirst()
                .map(modelSettingsMap::get)
                .orElse(null);
    }

    @SneakyThrows
    private Map<String, ModelSettings> readSettings() {
        return mapper.readValue(storage, new TypeReference<Map<String, ModelSettings>>() {
        });
    }

    @Override
    public synchronized ModelSettings getSettings(Class<?> modelClass) {
        return modelSettingsMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getModelClass() != null && entry.getValue().getModelClass().equals(modelClass))
                .findFirst()
                .map(modelSchemaIdModelSettingsEntry -> modelSettingsMap.get(modelSchemaIdModelSettingsEntry.getKey()))
                .orElse(null);
    }

    @SneakyThrows
    public synchronized void setSettings(String schemaId, ModelSettings modelSettings) {
        ModelSettings oldSettings = modelSettingsMap.get(schemaId);
        List<Diff> diffs = new ArrayList<>();
        oldSettings.getProperties().getVisible().forEach(
                s -> {
                    if (!modelSettings.getProperties().getVisible().contains(s)) {
                        diffs.add(new Diff(s, true));
                    }
                }
        );

        modelSettings.getProperties().getVisible().forEach(
                s -> {
                    if (!oldSettings.getProperties().getVisible().contains(s)) {
                        diffs.add(new Diff(s, false));
                    }
                }
        );

        String modelSchemaId = modelSettingsMap
                .keySet()
                .stream()
                .filter(m -> m.equals(schemaId))
                .findFirst()
                .orElseThrow(() -> new ModelNotFoundException(String.format("Model with schema %s not found", schemaId)));

        ModelSettings current = modelSettingsMap.get(modelSchemaId);
        modelSettings.setModelClass(current.getModelClass());
        log.info("Set new settings {} for model {}", modelSettings, schemaId);
        modelSettingsMap.put(modelSchemaId, modelSettings);
        Set<String> inheritorsId = schemas.stream()
                .filter(registeredSchema -> registeredSchema.getId().equals(modelSchemaId))
                .limit(1)
                .flatMap(registeredSchema -> registeredSchema.getInheritors().stream())
                .map(RegisteredSchema::getId)
                .collect(Collectors.toSet());

        Set<String> toAdd = diffs.stream().filter(diff -> !diff.removed).map(Diff::getProperty).collect(Collectors.toSet());
        Set<String> toRemove = diffs.stream().filter(diff -> diff.removed).map(Diff::getProperty).collect(Collectors.toSet());

        modelSettingsMap
                .entrySet()
                .stream()
                .filter(entry -> inheritorsId.contains(entry.getKey()))
                .forEach(entry -> {
                    entry.getValue().getProperties().getVisible().addAll(toAdd);
                    entry.getValue().getProperties().setVisible(entry.getValue().getProperties().getVisible().stream().filter(s -> !toRemove.contains(s)).collect(Collectors.toSet()));
                });

        mapper.writeValue(storage, modelSettingsMap);
        modelSettingMessageChannel.send(new GenericMessage<>(modelSettings));
    }

    @Data
    @AllArgsConstructor
    private static class Diff {
        private String property;
        private boolean removed;
    }


    @SneakyThrows
    public synchronized void loadSettings(Map<RegisteredSchema, ModelSettings> settingsMap) {
        modelSettingsMap = readSettings();
        if (modelSettingsMap.isEmpty()) {

            modelSettingsMap = settingsMap
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            registeredSchemaModelSettingsEntry -> registeredSchemaModelSettingsEntry.getKey().getId(),
                            Map.Entry::getValue)
                    );
            mapper.writeValue(storage, modelSettingsMap);
        }
        this.schemas = settingsMap.keySet();
    }
}