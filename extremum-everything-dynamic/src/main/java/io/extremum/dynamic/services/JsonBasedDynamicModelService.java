package io.extremum.dynamic.services;

import io.extremum.dynamic.models.impl.JsonDynamicModel;
import reactor.core.publisher.Mono;

public interface JsonBasedDynamicModelService extends DynamicModelService<JsonDynamicModel> {
    Mono<JsonDynamicModel> saveModelWithoutNotifications(JsonDynamicModel model);
}
