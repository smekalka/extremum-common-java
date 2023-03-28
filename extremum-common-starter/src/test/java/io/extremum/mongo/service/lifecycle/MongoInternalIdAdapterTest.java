package io.extremum.mongo.service.lifecycle;

import io.extremum.mongo.model.MongoCommonModel;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class MongoInternalIdAdapterTest {
    private final MongoInternalIdAdapter adapter = new MongoInternalIdAdapter();
    private final ObjectId objectId = new ObjectId();

    @Test
    void convertsInternalIdToString() {
        TestModel model = new TestModel();
        model.setId(objectId);

        assertThat(adapter.getInternalId(model).orElse("null"), equalTo(objectId.toString()));
    }

    @Test
    void returnsEmptyForNullInternalId() {
        TestModel model = new TestModel();
        model.setId(null);

        assertThat(adapter.getInternalId(model).orElse("null"), is("null"));
    }

    @Test
    void setsInternalId() {
        TestModel model = new TestModel();

        adapter.setInternalId(model, objectId.toString());

        assertThat(model.getId(), equalTo(objectId));
    }

    @Test
    void generatesNewInternalId() {
        String newInternalId = adapter.generateNewInternalId();

        assertThat(newInternalId, is(notNullValue()));
        assertThat(new ObjectId(newInternalId), not(equalTo(objectId)));
    }

    private static class TestModel extends MongoCommonModel {
    }
}