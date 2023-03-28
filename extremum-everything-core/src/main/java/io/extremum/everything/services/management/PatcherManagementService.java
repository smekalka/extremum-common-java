package io.extremum.everything.services.management;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import com.github.fge.jsonpatch.JsonPatch;

public interface PatcherManagementService {
    ResponseDto patch(Descriptor id, JsonPatch patch, boolean expand);
}
