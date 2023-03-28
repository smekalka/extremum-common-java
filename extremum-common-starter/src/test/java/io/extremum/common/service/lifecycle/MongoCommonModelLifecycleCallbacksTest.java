package io.extremum.common.service.lifecycle;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.impl.DescriptorIdResolver;
import io.extremum.common.descriptor.factory.impl.InMemoryDescriptorService;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.descriptors.common.AnnotationBasedStumpFacilities;
import io.extremum.descriptors.common.StumpFacilities;
import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.mongo.facilities.MongoDescriptorFacilitiesImpl;
import io.extremum.mongo.service.lifecycle.MongoCommonModelLifecycleCallbacks;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import models.TestMongoModel;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class MongoCommonModelLifecycleCallbacksTest {
    private MongoCommonModelLifecycleCallbacks callbacks;

    @Spy
    private final DescriptorService descriptorService = new InMemoryDescriptorService();

    private final ObjectId objectId = new ObjectId();
    private final Descriptor descriptor = Descriptor.builder()
            .externalId("existing-external-id")
            .internalId(objectId.toString())
            .modelType("Test")
            .storageType(StandardStorageType.MONGO)
            .build();

    @BeforeEach
    void createCallbacks() {
        MongoDescriptorFacilities facilities = new MongoDescriptorFacilitiesImpl(new DescriptorFactory(),
                new DescriptorSaver(descriptorService), new DescriptorIdResolver(descriptorService));
        callbacks = new MongoCommonModelLifecycleCallbacks(facilities, object -> new HashMap<>());
    }

    @Test
    void givenAnEntityHasNeitherIdNorUUID_whenItIsSaved_thenANewDescriptorShouldBeGeneratedWithNewObjectIdAndAssignedToUuidAndItsInternalIdAssignedToId() {
        alwaysGenerateExpectedExternalId();
        TestMongoModel model = new TestMongoModel();

        callbacks.onBeforeConvert(model, "does-not-matter");

        assertThatDescriptorWasGeneratedWithNewInternalId(model);
        assertThatDescriptorInternalIdMatchesEntityId(model);
        assertThatDescriptorWasSaved(model);
    }

    private void alwaysGenerateExpectedExternalId() {
        when(descriptorService.createExternalId()).thenReturn("new-external-id");
    }

    private void assertThatDescriptorWasGeneratedWithNewInternalId(TestMongoModel model) {
        assertThat(model.getUuid(), is(notNullValue()));
        assertThat(model.getUuid().getExternalId(), is("new-external-id"));
        assertThat(model.getUuid().getInternalId(), is(not(objectId.toString())));
        assertThat(model.getId(), is(notNullValue()));
    }

    private void assertThatDescriptorInternalIdMatchesEntityId(TestMongoModel model) {
        assertThat(model.getId().toString(), is(equalTo(model.getUuid().getInternalId())));
    }

    private void assertThatDescriptorWasSaved(TestMongoModel model) {
        verify(descriptorService).store(model.getUuid());
    }

    @Test
    void givenAnEntityHasNoIdButHasUUID_whenItIsSaved_thenDescriptorShouldNotBeGeneratedButUUIDsInternalIdAssignedToId() {
        TestMongoModel model = new TestMongoModel();
        model.setUuid(descriptor);

        callbacks.onBeforeConvert(model, "does-not-matter");

        assertThatUUIDWasNotChanged(model);
        assertThatEntityIdWasTakenFromUUID(model);
        assertThatNoDescriptorWasSaved();
    }

    private void assertThatUUIDWasNotChanged(TestMongoModel model) {
        assertThat(model.getUuid(), is(sameInstance(descriptor)));
    }

    private void assertThatEntityIdWasTakenFromUUID(TestMongoModel model) {
        assertThat(model.getId(), is(objectId));
    }

    private void assertThatNoDescriptorWasSaved() {
        verify(descriptorService, never()).store(any());
    }

    @Test
    void givenAnEntityHasIdButNoUUID_whenItIsSaved_thenANewDescriptorShouldBeGeneratedForThatIdAndAssignedToUuid() {
        alwaysGenerateExpectedExternalId();
        TestMongoModel model = new TestMongoModel();
        model.setId(objectId);

        callbacks.onBeforeConvert(model, "does-not-matter");

        assertThatDescriptorWasGeneratedWithGivenInternalId(model);
        assertThatEntityIdDidNotChange(model);
        assertThatDescriptorWasSaved(model);
    }

    private void assertThatDescriptorWasGeneratedWithGivenInternalId(TestMongoModel model) {
        assertThat(model.getUuid(), is(notNullValue()));
        assertThat(model.getUuid().getExternalId(), is("new-external-id"));
        assertThat(model.getUuid().getInternalId(), is(objectId.toString()));
    }

    private void assertThatEntityIdDidNotChange(TestMongoModel model) {
        assertThat(model.getId(), is(objectId));
    }

    @Test
    void givenAnEntityHasBothIdAndUUID_whenItIsSaved_thenNothingShouldHappen() {
        TestMongoModel model = new TestMongoModel();
        model.setId(objectId);
        model.setUuid(descriptor);

        callbacks.onBeforeConvert(model, "does-not-matter");

        assertThatUUIDWasNotChanged(model);
        assertThatEntityIdDidNotChange(model);
        assertThatNoDescriptorWasSaved();
    }
}