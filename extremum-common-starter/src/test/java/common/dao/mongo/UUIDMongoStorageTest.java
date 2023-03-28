package common.dao.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.extremum.common.test.TestWithServices;
import io.extremum.mongo.dbfactory.MongoDatabaseFactoryProperties;
import lombok.Data;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
class UUIDMongoStorageTest extends TestWithServices {
    @Autowired
    private ReactiveMongoOperations reactiveMongoOperations;

    @Autowired
    private MongoDatabaseFactoryProperties mongoProperties;

    @Test
    void shouldReturnEntityWithSameUUIDWhenSearchingViaReactiveMongoTemplate() {
        UUID uuid = UUID.randomUUID();
        EntityWithUUID savedEntity = saveEntityWithUUID(uuid);

        reactiveMongoOperations.findById(savedEntity.getId(), EntityWithUUID.class)
                .as(StepVerifier::create)
                .assertNext(foundEvent -> {
                    assertThat(foundEvent.getUuid(), is(uuid));
                })
                .verifyComplete();
    }

    private EntityWithUUID saveEntityWithUUID(UUID uuid) {
        EntityWithUUID entityToSave = new EntityWithUUID();
        entityToSave.setUuid(uuid);

        return reactiveMongoOperations.save(entityToSave).block();
    }

    @Test
    void shouldStoreUUIDAsStandardUUID() {
        UUID uuid = UUID.randomUUID();
        EntityWithUUID savedEntity = saveEntityWithUUID(uuid);

        Document document = getEntityDocumentDirectlyFromDriver(savedEntity.getId());

        assertThat(document, hasEntry("uuid", uuid));
        assertThat(document.get("uuid"), instanceOf(UUID.class));
    }

    private Document getEntityDocumentDirectlyFromDriver(ObjectId documentId) {
        try (MongoClient client = getClient()) {
            MongoDatabase database = client.getDatabase(mongoProperties.getServiceDbName());
            MongoCollection<Document> collection = database.getCollection("entity_with_uuid");
            List<Document> documents = collection.find(Filters.eq("_id", documentId))
                    .into(new ArrayList<>());

            assertThat(documents, hasSize(1));

            return documents.get(0);
        }
    }

    private MongoClient getClient() {
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoProperties.getUri()))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();
        return MongoClients.create(clientSettings);
    }

    @org.springframework.data.mongodb.core.mapping.Document("entity_with_uuid")
    @Data
    private static class EntityWithUUID {
        @Id
        private ObjectId id;
        private UUID uuid;
    }
}