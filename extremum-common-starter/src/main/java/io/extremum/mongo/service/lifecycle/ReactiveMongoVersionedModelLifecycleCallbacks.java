package io.extremum.mongo.service.lifecycle;

import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.mongo.model.MongoVersionedModel;

/**
 * @author rpuch
 */
public final class ReactiveMongoVersionedModelLifecycleCallbacks
        extends ReactiveMongoLifecycleCallbacks<MongoVersionedModel> {
    public ReactiveMongoVersionedModelLifecycleCallbacks(ReactiveMongoDescriptorFacilities mongoDescriptorFacilities) {
        super(mongoDescriptorFacilities, new MongoInternalIdAdapter());
    }
}
