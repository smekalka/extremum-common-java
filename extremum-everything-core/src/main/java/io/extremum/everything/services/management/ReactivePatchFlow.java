package io.extremum.everything.services.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.sharedmodels.basic.HasUuid;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Objects;

/**
 * @author rpuch
 */
public interface ReactivePatchFlow {
    String MODEL_BEING_PATCHED = "ReactivePatchFlow.modelBeingPatched";

    Mono<Model> patch(Descriptor id, JsonPatch patch);

    static Mono<Boolean> isPatching(Model model, Context context) {
        if (model instanceof HasUuid) {
            HasUuid hasUuid = (HasUuid) model;
            return hasUuid.getUuid().getInternalIdReactively()
                    .map(internalId -> Objects.equals(context.getOrDefault(MODEL_BEING_PATCHED, null), internalId));
        } else {
            return Mono.just(false);
        }
    }
}
