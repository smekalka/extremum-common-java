package io.extremum.everything.services.management;

import io.extremum.sharedmodels.dto.Response;
import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.descriptor.Descriptor;

public interface EverythingGetDemultiplexer {
    Response get(Descriptor id, Projection projection, boolean expand);
}
