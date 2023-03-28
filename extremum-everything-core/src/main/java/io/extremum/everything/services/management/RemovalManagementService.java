package io.extremum.everything.services.management;

import io.extremum.sharedmodels.descriptor.Descriptor;

public interface RemovalManagementService {
    void remove(Descriptor id);

    void remove(Descriptor id, Descriptor nestedId);
}
