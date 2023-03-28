package io.extremum.schema.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.spring.data.OffsetBasedPageRequest;
import io.extremum.everything.services.models.ModelSettingsProvider;
import io.extremum.schema.model.ModelSchema;
import io.extremum.sharedmodels.schema.RegisteredSchema;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ExtremumModelSchemaService {

    private final String schemaPath;

    private final ModelSchemaRegistrar registrar;
    private final ModelSettingsProvider modelSettingsProvider;
    private final ObjectMapper objectMapper;

    public Page<ModelSchema> getModels(boolean expand, long limit, long offset, String prefix) {

        List<ModelSchema> modelSchemas = registrar.getModelSchemas()
                .stream()
                .filter(registeredSchema -> registeredSchema.getId().startsWith(prefix))
                .skip(offset)
                .limit(limit)
                .map(registeredSchema -> new ModelSchema(
                        registeredSchema.getId(),
                        getSchemaJsonNode(registeredSchema).orElse(NullNode.getInstance()),
                        modelSettingsProvider.getSettings(registeredSchema.getId())

                ))
                .collect(Collectors.toList());

        return new PageImpl<>(modelSchemas, new OffsetBasedPageRequest(offset, (int) limit), registrar.getModelSchemas().size());
    }

    private Optional<JsonNode> getSchemaJsonNode(RegisteredSchema registeredSchema) {
        try {
            return Optional.of(objectMapper.readTree(new File(schemaPath + registeredSchema.getId())));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public Optional<ModelSchema> getModel(String model) {
        return Optional.ofNullable(registrar
                .getModelSchemas()
                .stream()
                .filter(registeredSchema -> registeredSchema.getId().equals(model))
                .map(registeredSchema -> new ModelSchema(
                        registeredSchema.getId(),
                        getSchemaJsonNode(registeredSchema).orElse(NullNode.getInstance()),
                        modelSettingsProvider.getSettings(registeredSchema.getId())
                ))
                .findFirst()
                .orElseThrow(() -> new ModelNotFoundException("Model " + model + " not found")));
    }

    @SneakyThrows
    public Optional<ModelSchema> patchModel(String modelPath, JsonPatch patch) {
        Optional<ModelSchema> existing = getModel(modelPath);
        Optional<JsonNode> patchedNode = existing.map(
                model -> {
                    try {

                        return patch.apply(objectMapper.valueToTree(model));
                    } catch (JsonPatchException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        Optional<ModelSchema> modelSchema = patchedNode.map(node -> objectMapper.convertValue(node, ModelSchema.class));
        modelSchema.ifPresent(modelSchema1 -> {
            existing.ifPresent(schema -> modelSchema1.getSettings().getProperties().setAll(schema.getSettings().getProperties().getAll()));
            modelSettingsProvider.setSettings(modelPath, modelSchema1.getSettings());
        });
        return modelSchema;
    }
}
