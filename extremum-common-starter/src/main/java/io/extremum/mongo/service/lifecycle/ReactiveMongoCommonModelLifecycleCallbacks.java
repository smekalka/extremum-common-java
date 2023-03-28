package io.extremum.mongo.service.lifecycle;

import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.mongo.model.MongoCommonModel;

/**
 * @author rpuch
 */
public final class ReactiveMongoCommonModelLifecycleCallbacks
        extends ReactiveMongoLifecycleCallbacks<MongoCommonModel> {
    public ReactiveMongoCommonModelLifecycleCallbacks(ReactiveMongoDescriptorFacilities mongoDescriptorFacilities) {
        super(mongoDescriptorFacilities, new MongoInternalIdAdapter());
    }
}