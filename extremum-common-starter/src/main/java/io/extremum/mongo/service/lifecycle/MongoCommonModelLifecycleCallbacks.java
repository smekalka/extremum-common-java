package io.extremum.mongo.service.lifecycle;

import io.extremum.common.utils.ModelUtils;
import io.extremum.descriptors.common.StumpFacilities;
import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveCallback;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;

public final class MongoCommonModelLifecycleCallbacks implements BeforeConvertCallback<MongoCommonModel>,
        AfterSaveCallback<MongoCommonModel>, AfterConvertCallback<MongoCommonModel> {
    private final MongoDescriptorFacilities mongoDescriptorFacilities;
    private final StumpFacilities stumpFacilities;

    public MongoCommonModelLifecycleCallbacks(MongoDescriptorFacilities mongoDescriptorFacilities, StumpFacilities stumpFacilities) {
        this.mongoDescriptorFacilities = mongoDescriptorFacilities;
        this.stumpFacilities = stumpFacilities;
    }

    @Override
    public MongoCommonModel onBeforeConvert(MongoCommonModel entity, String collection) {
        fillRequiredFields(entity);
        return entity;
    }

    private void fillRequiredFields(MongoCommonModel model) {
        final boolean internalIdGiven = model.getId() != null;
        final boolean uuidGiven = model.getUuid() != null;

        if (uuidGiven && !internalIdGiven) {
            model.setId(getInternalIdFromDescriptor(model));
        } else if (!uuidGiven && internalIdGiven) {
            Descriptor descriptor = createAndSaveDescriptorWithGivenInternalId(model.getId(), model);
            model.setUuid(descriptor);
        } else if (!uuidGiven && !internalIdGiven) {
            Descriptor descriptor = createAndSaveDescriptorWithGivenInternalId(newEntityId(), model);
            model.setUuid(descriptor);
            model.setId(getInternalIdFromDescriptor(model));
        }
    }

    private ObjectId getInternalIdFromDescriptor(MongoCommonModel model) {
        return mongoDescriptorFacilities.resolve(model.getUuid());
    }

    private Descriptor createAndSaveDescriptorWithGivenInternalId(ObjectId objectId, MongoCommonModel model) {
        String modelName = ModelUtils.getModelName(model);
        String iri = model.getIri();
        return mongoDescriptorFacilities.create(objectId, modelName, iri, stumpFacilities.getStump(model));
    }

    private ObjectId newEntityId() {
        return new ObjectId();
    }

    @Override
    public MongoCommonModel onAfterSave(MongoCommonModel entity, Document document, String collection) {
        createDescriptorIfNeeded(entity);
        return entity;
    }

    private void createDescriptorIfNeeded(MongoCommonModel model) {
        if (model.getUuid() == null) {
            String name = ModelUtils.getModelName(model.getClass());
            String iri = model.getIri();
            model.setUuid(mongoDescriptorFacilities.create(model.getId(), name, iri, stumpFacilities.getStump(model)));
        }
    }

    @Override
    public MongoCommonModel onAfterConvert(MongoCommonModel entity, Document document, String collection) {
        resolveDescriptor(entity);
        return entity;
    }

    private void resolveDescriptor(MongoCommonModel model) {
        model.setUuid(mongoDescriptorFacilities.fromInternalId(model.getId()));
    }
}
