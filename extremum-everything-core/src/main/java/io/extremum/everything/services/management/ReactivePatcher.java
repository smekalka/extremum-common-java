package io.extremum.everything.services.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactivePatcher {
    Mono<Model> patch(Descriptor id, Model modelToPatch, JsonPatch patch);
}
