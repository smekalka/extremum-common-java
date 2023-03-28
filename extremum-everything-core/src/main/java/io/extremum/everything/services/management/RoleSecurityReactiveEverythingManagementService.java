package io.extremum.everything.services.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.security.ReactiveRoleSecurity;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RoleSecurityReactiveEverythingManagementService implements ReactiveEverythingManagementService {
    private final ReactiveEverythingManagementService everythingService;
    private final ReactiveRoleSecurity roleSecurity;

    @Override
    public Mono<ResponseDto> get(Descriptor id, boolean expand) {
        return roleSecurity.checkGetAllowed(id)
                .then(everythingService.get(id, expand));
    }

    @Override
    public Mono<ResponseDto> patch(Descriptor id, JsonPatch patch, boolean expand) {
        return roleSecurity.checkPatchAllowed(id)
                .then(everythingService.patch(id, patch, expand));
    }

    @Override
    public Mono<Void> remove(Descriptor id) {
        return roleSecurity.checkRemovalAllowed(id)
                .then(everythingService.remove(id));
    }
}
