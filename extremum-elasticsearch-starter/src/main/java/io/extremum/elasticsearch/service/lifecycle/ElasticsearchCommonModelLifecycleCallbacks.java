package io.extremum.elasticsearch.service.lifecycle;

import io.extremum.common.utils.ModelUtils;
import io.extremum.elasticsearch.facilities.ElasticsearchDescriptorFacilities;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.event.AfterConvertCallback;
import org.springframework.data.elasticsearch.core.event.AfterSaveCallback;
import org.springframework.data.elasticsearch.core.event.BeforeConvertCallback;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import java.util.UUID;

public final class ElasticsearchCommonModelLifecycleCallbacks implements
        BeforeConvertCallback<ElasticsearchCommonModel>,
        AfterSaveCallback<ElasticsearchCommonModel>,
        AfterConvertCallback<ElasticsearchCommonModel> {
    private final ElasticsearchDescriptorFacilities descriptorFacilities;

    public ElasticsearchCommonModelLifecycleCallbacks(ElasticsearchDescriptorFacilities descriptorFacilities) {
        this.descriptorFacilities = descriptorFacilities;
    }

    @Override
    public ElasticsearchCommonModel onBeforeConvert(ElasticsearchCommonModel entity, IndexCoordinates index) {
        fillRequiredFields(entity);
        return entity;
    }

    private void fillRequiredFields(ElasticsearchCommonModel model) {
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

    private String getInternalIdFromDescriptor(ElasticsearchCommonModel model) {
        return descriptorFacilities.resolve(model.getUuid()).toString();
    }

    private Descriptor createAndSaveDescriptorWithGivenInternalId(String modelId, ElasticsearchCommonModel model) {
        String modelName = ModelUtils.getModelName(model);
        String iri = model.getIri();
        return descriptorFacilities.create(UUID.fromString(modelId), modelName, iri);
    }

    private String newEntityId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public ElasticsearchCommonModel onAfterSave(ElasticsearchCommonModel entity, IndexCoordinates index) {
        createDescriptorIfNeeded(entity);
        return entity;
    }

    private void createDescriptorIfNeeded(ElasticsearchCommonModel model) {
        if (model.getUuid() == null) {
            String name = ModelUtils.getModelName(model.getClass());
            String iri = model.getIri();
            model.setUuid(descriptorFacilities.create(UUID.fromString(model.getId()), name, iri));
        }
    }

    @Override
    public ElasticsearchCommonModel onAfterConvert(ElasticsearchCommonModel entity, Document document,
            IndexCoordinates indexCoordinates) {
        resolveDescriptor(entity);
        return entity;
    }

    private void resolveDescriptor(ElasticsearchCommonModel model) {
        model.setUuid(descriptorFacilities.fromInternalId(model.getId()));
    }
}
