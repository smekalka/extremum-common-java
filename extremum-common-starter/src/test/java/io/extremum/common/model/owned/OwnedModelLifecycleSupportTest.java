package io.extremum.common.model.owned;

import io.extremum.common.model.owned.model.OwnedModel;
import io.extremum.mongo.model.MongoCommonModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

class OwnedModelLifecycleSupportTest {

    private final OwnedModelLifecycleSupport ownedModelLifecycleSupport = new OwnedModelLifecycleSupport();

    @Test
    public void Should_get_owned_models_from_folder_model() {
        ModelWithOwnedFields model = new ModelWithOwnedFields(
                 new OwnedFieldModel()
        );

        List<Field> ownedModelsFromFolder = ownedModelLifecycleSupport.getOwnedFields(model);

        Assertions.assertEquals(1, ownedModelsFromFolder.size());
        Assertions.assertEquals("fieldC", ownedModelsFromFolder.get(0).getName());
    }

    @AllArgsConstructor
    @Getter
    private static class ModelWithOwnedFields extends MongoCommonModel {

        private final OwnedFieldModel fieldC;
    }

    private static class OwnedFieldModel extends OwnedModel {
    }
}