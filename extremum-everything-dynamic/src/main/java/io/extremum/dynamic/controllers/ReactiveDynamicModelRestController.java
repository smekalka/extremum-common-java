package io.extremum.dynamic.controllers;

import io.extremum.dynamic.SchemaMetaService;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.services.JsonBasedDynamicModelService;
import io.extremum.everything.controllers.EverythingExceptionHandlerTarget;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@EverythingExceptionHandlerTarget
public class ReactiveDynamicModelRestController implements DynamicModelRestController {
    private final JsonBasedDynamicModelService dynamicModelService;
    private final SchemaMetaService schemaMetaService;
    private final NetworkntSchemaProvider schemaProvider;

    @PostMapping("/models/{modelName}")
    public Mono<Response> createDynamicModel(@PathVariable String modelName, @RequestBody Map<String, Object> data) {
        return dynamicModelService.saveModel(new JsonDynamicModel(modelName, data))
                .map(Response::ok)
                .onErrorResume(this::makeErrorResponse);
    }

    private Mono<Response> makeErrorResponse(Throwable throwable) {
        return Mono.fromSupplier(() -> Response.fail(Alert.errorAlert(throwable.getMessage(), null, "xyz-0001"), 400));
    }

    @GetMapping("/models")
    public Mono<Response> getRegisteredModels() {
        return Mono.just(schemaMetaService.getRegisteredSchemas())
                .map(Response::ok);
    }

    @GetMapping("/models/{name}")
    public Mono<Response> getNetworkNtSchema(@PathVariable String name) {
        return Mono.just(schemaProvider.loadSchema(getSchemaFileName(name)).getSchema().getSchemaNode())
                .onErrorResume(e -> Mono.error(new IllegalArgumentException("Illegal schema name")))
                .map(Response::ok);
    }

    private static String getSchemaFileName(String name) {
        String[] split = name.split("\\.");
        String model = split[0];
        String version = split[1];

        return model + (version.equals("v1") ? "" : "." + version) + ".json";
    }
}