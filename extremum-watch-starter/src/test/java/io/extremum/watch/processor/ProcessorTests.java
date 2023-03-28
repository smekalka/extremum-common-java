package io.extremum.watch.processor;

import io.extremum.common.utils.ModelUtils;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.watch.models.ModelMetadata;
import io.extremum.watch.models.TextWatchEvent;
import org.bson.types.ObjectId;

import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class ProcessorTests {
    private static final String EXTERNAL_ID = "external-id";
    static final ZonedDateTime CREATED = ZonedDateTime.now().minusDays(1);
    static final ZonedDateTime LAST_MODIFIED = CREATED.minusDays(1);
    static final long VERSION = 10L;
    static final String WATCHED_MODEL_NAME = "WatchedModel";
    static final String NON_WATCHED_MODEL_NAME = "NonWatchedModel";

    static Descriptor descriptor(ObjectId id, String modelType) {
        return Descriptor.builder()
                .externalId(EXTERNAL_ID)
                .internalId(id.toString())
                .modelType(modelType)
                .storageType(StandardStorageType.MONGO)
                .build();
    }

    static void assertThatEventModelIdMatchesModelId(WatchedModel model, TextWatchEvent event) {
        assertThat(event.getModelId(), is(equalTo(model.getId().toString())));
    }

    static void assertThatEventMetadataMatchesModelMetadataFully(WatchedModel model,
            TextWatchEvent event) {
        assertThatEventMetadataMatchesModelMetadataExceptModifiedField(event, model);
        assertThat(event.getModelMetadata().getModified(), is(equalTo(LAST_MODIFIED)));
    }

    static void assertThatEventMetadataMatchesModelMetadataExceptModifiedField(
            TextWatchEvent event, FilledModel model) {
        ModelMetadata eventMetadata = event.getModelMetadata();
        assertThat(eventMetadata.getId(), is(EXTERNAL_ID));
        assertThat(eventMetadata.getModel(), is(equalTo(ModelUtils.getModelName(model))));
        assertThat(eventMetadata.getCreated(), is(equalTo(CREATED)));
        assertThat(eventMetadata.getModified(), is(notNullValue()));
        assertThat(eventMetadata.getVersion(), is(equalTo(VERSION)));
    }
}
