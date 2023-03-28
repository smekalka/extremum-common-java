package io.extremum.jpa.service.lifecycle;

import io.extremum.common.utils.ModelUtils;
import io.extremum.descriptors.common.StumpFacilities;
import io.extremum.jpa.facilities.PostgresDescriptorFacilities;
import io.extremum.jpa.facilities.StaticPostgresDescriptorFacilitiesAccessor;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import java.util.UUID;

public class JpaCommonModelLifecycleListener {

    private static StumpFacilities stumpFacilities;

    @Autowired
    public void setStumpStumpFacilities(StumpFacilities stumpFacilities) {
        JpaCommonModelLifecycleListener.stumpFacilities = stumpFacilities;
    }

    @PrePersist
    public void fillRequiredFields(BasicModel<UUID> model) {
        ensureFacilitiesAreAvailable();

        final boolean internalIdGiven = model.getId() != null;
        final boolean uuidGiven = model.getUuid() != null;

        if (uuidGiven && !internalIdGiven) {
            model.setId(getInternalIdFromDescriptor(model));
        } else if (!uuidGiven && internalIdGiven) {
            Descriptor descriptor = createAndSaveDescriptorWithGivenInternalId(model.getId(), model);
            model.setUuid(descriptor);
        } else if (!uuidGiven) {
            Descriptor descriptor = createAndSaveDescriptorWithGivenInternalId(newEntityId(), model);
            model.setUuid(descriptor);
            model.setId(getInternalIdFromDescriptor(model));
        }
    }

    @PostUpdate
    public void postUpdate(BasicModel<UUID> model) {
        Descriptor descriptor = model.getUuid();
        descriptor.setStump(stumpFacilities.getStump(model));
        descriptorFacilities().save(descriptor);
    }

    private void ensureFacilitiesAreAvailable() {
        if (descriptorFacilities() == null) {
            throw new IllegalStateException("PostgresqlDescriptorFacilities is not available");
        }
    }

    private PostgresDescriptorFacilities descriptorFacilities() {
        return StaticPostgresDescriptorFacilitiesAccessor.getFacilities();
    }

    private UUID getInternalIdFromDescriptor(BasicModel<UUID> model) {
        return descriptorFacilities().resolve(model.getUuid());
    }

    private Descriptor createAndSaveDescriptorWithGivenInternalId(UUID internalId, BasicModel<UUID> model) {
        String modelName = ModelUtils.getModelName(model);
        String iri = model.getIri();
        return descriptorFacilities().create(internalId, modelName, iri, stumpFacilities.getStump(model));
    }

    private UUID newEntityId() {
        return UUID.randomUUID();
    }

    @PostPersist
    public void createDescriptorIfNeeded(BasicModel<UUID> model) {
        ensureFacilitiesAreAvailable();

        if (model.getUuid() == null) {
            model.setUuid(descriptorFacilities().create(model.getId(), ModelUtils.getModelName(model), model.getIri(), stumpFacilities.getStump(model)));
        }
    }

    @PostLoad
    public void onAfterConvert(BasicModel<UUID> model) {
        ensureFacilitiesAreAvailable();

        model.setUuid(descriptorFacilities().fromInternalId(model.getId()));
    }
}
