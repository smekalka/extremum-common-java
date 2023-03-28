package common.dao.mongo;

import io.extremum.common.test.TestWithServices;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
class MongoPolymorphicFieldsPersistenceTest extends TestWithServices {
    @Autowired
    private MongoOperations mongoOperations;

    @Test
    void givenAnEntityIsSavedWithAPolymorhpicFieldAnnotatedWithTypeAlias_whenRestoringHostObject_thenFieldShouldBeRestoredWithOriginalClass() {
        EntityWithPolymorphicField model = saveModelWith(new AnnotatedContent("the content"));

        EntityWithPolymorphicField retrievedModel = mongoOperations.findById(model.getId(),
                EntityWithPolymorphicField.class);

        assertThat(retrievedModel, is(notNullValue()));
        assertThat(retrievedModel.getPolymorphicField(), instanceOf(AnnotatedContent.class));
    }

    @NotNull
    private EntityWithPolymorphicField saveModelWith(Object content) {
        EntityWithPolymorphicField model = new EntityWithPolymorphicField();
        model.setPolymorphicField(content);
        mongoOperations.save(model);
        return model;
    }

    @Test
    void givenAnEntityIsSavedWithAPolymorhpicFieldNotAnnotatedWithTypeAlias_whenRestoringHostObject_thenFieldShouldBeRestoredAsAMap() {
        EntityWithPolymorphicField model = saveModelWith(new NotAnnotatedContent("the content"));

        EntityWithPolymorphicField retrievedModel = mongoOperations.findById(model.getId(),
                EntityWithPolymorphicField.class);

        assertThat(retrievedModel, is(notNullValue()));
        assertThat(retrievedModel.getPolymorphicField(), instanceOf(Map.class));
    }
}
