package io.extremum.watch.controller;

import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.common.annotation.ModelName;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.types.ObjectId;

import java.time.ZonedDateTime;

/**
 * @author rpuch
 */
@ModelName("ModelWithExpectedValues")
public class ModelWithFilledValues extends MongoCommonModel {
    public ModelWithFilledValues() {
        setId(new ObjectId());
        setUuid(Descriptor.builder()
                .externalId("external-id")
                .internalId(getId().toString())
                .build());
        setCreated(ZonedDateTime.now());
        setModified(ZonedDateTime.now());
        setVersion(1L);
    }
}
