package io.extremum.everything.services.models;

import io.extremum.sharedmodels.basic.ModelSettings;
import io.extremum.sharedmodels.schema.RegisteredSchema;

import java.util.Map;

public interface ModelSettingsProvider {
    ModelSettings getSettings(String fullName);

    ModelSettings getSettings(Class<?> modelClass);

    void setSettings(String fullName, ModelSettings modelSettings);

    void loadSettings(Map<RegisteredSchema, ModelSettings> modelSettingsMap);
}