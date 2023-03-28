package io.extremum.everything.services.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.common.model.CollectionFilter;
import io.extremum.everything.collection.Projection;
import io.extremum.security.RoleSecurity;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RoleSecurityEverythingEverythingManagementService implements EverythingEverythingManagementService {
    private final EverythingEverythingManagementService everythingService;
    private final RoleSecurity roleSecurity;

    @Override
    public ResponseDto get(Descriptor id, boolean expand) {
        roleSecurity.checkGetAllowed(id);

        return everythingService.get(id, expand);
    }

    @Override
    public Response get(String iri, Projection projection, boolean expand) {
        return everythingService.get(iri, projection, expand);
    }

    @Override
    public ResponseDto patch(Descriptor id, JsonPatch patch, boolean expand) {
        roleSecurity.checkPatchAllowed(id);

        return everythingService.patch(id, patch, expand);
    }

    @Override
    public void remove(Descriptor id) {
        roleSecurity.checkRemovalAllowed(id);

        everythingService.remove(id);
    }

    @Override
    public ResponseDto create(String modelName, RequestDto requestDto, boolean expand) {
        return everythingService.create(modelName, requestDto, expand);
    }

    @Override
    public Response get(String iri, Projection projection, boolean expand, CollectionFilter collectionFilter) {
        return everythingService.get(iri, projection, expand, collectionFilter);
    }

    @Override
    public void remove(Descriptor collectionId, Descriptor nestedId) {
        roleSecurity.checkPatchAllowed(collectionId.getCollection().getCoordinates().getOwnedCoordinates().getHostId());
        everythingService.remove(collectionId, nestedId);
    }
}
