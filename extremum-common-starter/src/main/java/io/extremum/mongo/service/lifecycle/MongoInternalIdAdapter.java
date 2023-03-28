package io.extremum.mongo.service.lifecycle;

import io.extremum.common.lifecycle.InternalIdAdapter;
import io.extremum.sharedmodels.basic.BasicModel;
import org.bson.types.ObjectId;

import java.util.Objects;
import java.util.Optional;

public class MongoInternalIdAdapter implements InternalIdAdapter<BasicModel<ObjectId>> {
    @Override
    public Optional<String> getInternalId(BasicModel<ObjectId> model) {
        return Optional.ofNullable(model.getId())
                .map(ObjectId::toString);
    }

    @Override
    public void setInternalId(BasicModel<ObjectId> model, String internalId) {
        Objects.requireNonNull(internalId, "internalId must not be null");

        model.setId(new ObjectId(internalId));
    }

    @Override
    public String generateNewInternalId() {
        return new ObjectId().toString();
    }
}
