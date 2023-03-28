package io.extremum.everything.services.management;

import io.extremum.sharedmodels.dto.Response;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EverythingGetDemultiplexerOnDescriptor implements EverythingGetDemultiplexer {
    private final EverythingEverythingManagementService evrEvrManagementService;
    private final EverythingCollectionManagementService collectionManagementService;

    @Override
    public Response get(Descriptor id, Projection projection, boolean expand) {
        switch (id.effectiveType()) {
            case SINGLE:
                Object result = evrEvrManagementService.get(id, expand);
                return Response.ok(result);
            case COLLECTION:
                return fetchCollection(id, projection, expand);
            default:
                throw new EverythingEverythingException(
                        String.format("'%s' is neither single nor collection", id.getExternalId()));
        }
    }

    private Response fetchCollection(Descriptor id, Projection projection, boolean expand) {
        return collectionManagementService.fetchCollection(id, projection, expand);
    }
}
