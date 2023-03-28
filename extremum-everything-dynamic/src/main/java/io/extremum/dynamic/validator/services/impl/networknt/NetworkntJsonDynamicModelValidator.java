package io.extremum.dynamic.validator.services.impl.networknt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ImpermanentValidationContext;
import com.networknt.schema.ValidationMessage;
import io.atlassian.fugue.Try;
import io.extremum.dynamic.SchemaMetaService;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.validator.ValidationContext;
import io.extremum.dynamic.validator.Violation;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.atlassian.fugue.Try.failure;
import static io.atlassian.fugue.Try.successful;
import static reactor.core.publisher.Mono.defer;
import static reactor.core.publisher.Mono.just;

@Slf4j
@RequiredArgsConstructor
public class NetworkntJsonDynamicModelValidator implements JsonDynamicModelValidator {
    private final NetworkntSchemaProvider schemaProvider;
    private final ObjectMapper mapper;
    private final SchemaMetaService schemaMetaService;

    @Override
    public Mono<Try<ValidationContext>> validate(JsonDynamicModel model) {
        return defer(() -> {
            try {
                String schemaName = schemaMetaService.getSchemaName(model.getSchemaName(), model.getSchemaVersion());

                if (schemaName == null) {
                    throw new SchemaLoadingException("Unable to determine a schema name for model " + model.getModelName());
                }

                NetworkntSchema schema = schemaProvider.loadSchema(schemaName);

                Set<String> paths = new HashSet<>();

                Set<ValidationMessage> validationMessages = schema.getSchema().validate(
                        toJsonNode(model.getModelData()), createCtx(paths));

                if (!validationMessages.isEmpty()) {
                    DynamicModelValidationException ex = new DynamicModelValidationException(toViolationSet(validationMessages));
                    log.warn("Model {} is invalid", model, ex);
                    return just(failure(ex));
                } else {
                    return just(successful(new ValidationContext(paths)));
                }
            } catch (SchemaLoadingException e) {
                log.error("Unable to validate a model {}: schema not found", model, e);
                return just(failure(e));
            }
        });
    }

    private JsonNode toJsonNode(Map<String, Object> data) {
        return mapper.convertValue(data, JsonNode.class);
    }

    private ImpermanentValidationContext createCtx(Set<String> paths) {
        return new DefaultImpermanentValidationContext(paths);
    }

    private Set<Violation> toViolationSet(Set<ValidationMessage> messages) {
        return messages.stream()
                .map(msg -> (Violation) msg::getMessage)
                .collect(Collectors.toSet());
    }
}
