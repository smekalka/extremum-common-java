package common.dao.mongo;

import io.extremum.common.test.TestWithServices;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author rpuch
 */
@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
class MongoTypeInfoPersistenceTest extends TestWithServices {
    @Autowired
    private MongoOperations mongoOperations;

    @Test
    void whenSavingAnEntityWithoutTypeAlias_thenClassAttributeShouldNotBeSaved() {
        EntityWithoutTypeAlias model = new EntityWithoutTypeAlias();
        mongoOperations.save(model);

        Document document = findExactlyOneDocumentById(EntityWithoutTypeAlias.COLLECTION, model.getId());
        assertThat(document.get("_class"), is(nullValue()));
    }

    private Document findExactlyOneDocumentById(String collectionName, ObjectId entityId) {
        List<Document> documents = mongoOperations.getCollection(collectionName)
                .find(Filters.eq("_id", entityId))
                .into(new ArrayList<>());
        assertThat(documents, hasSize(1));
        return documents.get(0);
    }

    @Test
    void whenSavingAnEntityWithTypeAlias_thenClassAttributeShouldBeFilledWithTypeAlias() {
        EntityWithTypeAlias model = new EntityWithTypeAlias();
        mongoOperations.save(model);

        Document document = findExactlyOneDocumentById(EntityWithTypeAlias.COLLECTION, model.getId());
        assertThat(document.get("_class"), is("the-alias"));
    }
}
