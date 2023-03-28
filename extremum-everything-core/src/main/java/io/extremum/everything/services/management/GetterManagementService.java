package io.extremum.everything.services.management;

import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;

public interface GetterManagementService {
    ResponseDto get(Descriptor id, boolean expand);

    Response get(String iri, Projection projection, boolean expand);
}
