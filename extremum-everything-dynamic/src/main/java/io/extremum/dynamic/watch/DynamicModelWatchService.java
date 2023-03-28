package io.extremum.dynamic.watch;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import reactor.core.publisher.Mono;

public interface DynamicModelWatchService {
    Mono<Void> registerSaveOperation(JsonDynamicModel saved);

    Mono<Void> registerDeleteOperation(JsonDynamicModel model);

    Mono<Void> registerPatchOperation(JsonPatch patch, JsonDynamicModel mdoel);
}
