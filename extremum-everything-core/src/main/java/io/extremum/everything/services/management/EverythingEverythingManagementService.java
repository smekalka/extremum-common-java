package io.extremum.everything.services.management;

import io.extremum.common.model.CollectionFilter;
import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.dto.Response;

public interface EverythingEverythingManagementService extends
        GetterManagementService, PatcherManagementService, RemovalManagementService, CreateManagementService {
    Response get(String iri, Projection projection, boolean expand, CollectionFilter collectionFilter);
}
