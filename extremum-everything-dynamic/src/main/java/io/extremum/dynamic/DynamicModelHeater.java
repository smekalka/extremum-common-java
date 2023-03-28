package io.extremum.dynamic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.extremum.dynamic.config.DynamicModelProperties;
import io.extremum.dynamic.events.DynamicModelRegisteredEvent;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(200)
// DynamicModelHeater must launch directly after io.extremum.dynamic.HttpSchemaServerLauncher in a case with "dynmodels.schema.location=local"
@Component
@RequiredArgsConstructor
public class DynamicModelHeater implements ApplicationListener<ContextRefreshedEvent> {
    private final NetworkntSchemaProvider schemaProvider;
    private final DynamicModelProperties props;

    private volatile boolean alreadyHeated = false;
    private final SchemaMetaService schemaMetaService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (heatingAllowed()) {
            alreadyHeated = true;

            if (isSchemaPlacedOnLocal()) {
                log.info("Schema will be loaded from local area");
            } else {
                log.info("Schema will be loaded from github");
            }

            String schemaName = props.getSchema().getPointer().getSchemaName();
            int schemaVersion = props.getSchema().getPointer().getSchemaVersion();
            heatSchema(schemaName, schemaVersion);
        }
    }

    private void heatSchema(String schemaName, int schemaVersion) {
        try {
            NetworkntSchema loaded = schemaProvider.loadSchema(schemaName);
            JsonNode title = loaded.getSchema().getSchemaNode().get("title");
            if (!isTextNode(title)) {
                log.warn("No 'title' attribute found in schema {}. Model name for that schema can't be registered " +
                        "in a descriptor determinator", loaded.getSchema());
            } else {
                schemaMetaService.registerMapping(title.textValue(), schemaName, schemaVersion);
                onMappingRegistered(title.textValue());
            }
        } catch (SchemaLoadingException e) {
            log.error("Unable to load schema {}", schemaName, e);
        }
    }

    private boolean isSchemaPlacedOnLocal() {
        return DynamicModelProperties.Schema.Location.local.equals(props.getSchema().getLocation());
    }

    private boolean heatingAllowed() {
        return !alreadyHeated;
    }

    private void onMappingRegistered(String modelName) {
        eventPublisher.publishEvent(new DynamicModelRegisteredEvent(modelName));
    }

    private boolean isTextNode(JsonNode title) {
        return title instanceof TextNode;
    }
}
