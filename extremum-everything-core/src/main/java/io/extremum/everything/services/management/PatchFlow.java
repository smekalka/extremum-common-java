package io.extremum.everything.services.management;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import com.github.fge.jsonpatch.JsonPatch;

/**
 * @author rpuch
 */
public interface PatchFlow {
    Model patch(Descriptor id, JsonPatch patch);
}
