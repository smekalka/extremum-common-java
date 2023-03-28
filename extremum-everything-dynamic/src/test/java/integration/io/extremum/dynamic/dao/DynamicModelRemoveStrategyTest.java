package integration.io.extremum.dynamic.dao;

import integration.SpringBootTestWithServices;
import io.extremum.dynamic.dao.DynamicModelRemoveStrategy;
import io.extremum.dynamic.dao.HardDeleteRemoveStrategy;
import io.extremum.dynamic.dao.MongoDynamicModelDao;
import io.extremum.dynamic.dao.SoftDeleteRemoveStrategy;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.sharedmodels.basic.Model;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Mono;

import static io.extremum.dynamic.DynamicModelSupports.*;
import static io.extremum.dynamic.utils.DynamicModelTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SoftDeleteRemoveStrategyDaoTestConfigurations.class)
public class DynamicModelRemoveStrategyTest extends SpringBootTestWithServices {
    @Autowired
    ReactiveMongoOperations mongoOperations;

    @Autowired
    private ReactiveMongoDescriptorFacilities facilities;

    @Test
    void softDeleteTest() {
        JsonDynamicModel model = buildModel("AModelForSoftDelete", toMap("{\"f\": \"v\"}"));
        String collectionName = collectionNameFromModel(model.getModelName());

        DynamicModelRemoveStrategy strategy = createSoftDeleteStrategy();

        MongoDynamicModelDao dao = new MongoDynamicModelDao(mongoOperations, facilities, strategy);

        JsonDynamicModel persisted = dao.create(model, collectionName).block();

        strategy.remove(persisted.getId(), collectionName).block();

        Document found = findDocument(collectionName, persisted);

        assertNotNull(found);
        assertEquals(true, found.get(Model.FIELDS.deleted.name()));
    }

    @Test
    void hardDeleteTest() {
        JsonDynamicModel model = buildModel("AModelForHardDelete", toMap("{\"f\": \"v\"}"));
        String collectionName = collectionNameFromModel(model.getModelName());

        DynamicModelRemoveStrategy strategy = createHardDeleteStrategy();

        MongoDynamicModelDao dao = new MongoDynamicModelDao(mongoOperations, facilities, strategy);

        JsonDynamicModel persisted = dao.create(model, collectionName).block();

        strategy.remove(persisted.getId(), collectionName).block();

        Document found = findDocument(collectionName, persisted);

        assertNull(found);
    }

    @NotNull
    private SoftDeleteRemoveStrategy createSoftDeleteStrategy() {
        return new SoftDeleteRemoveStrategy(mongoOperations);
    }

    @NotNull
    private HardDeleteRemoveStrategy createHardDeleteStrategy() {
        return new HardDeleteRemoveStrategy(mongoOperations);
    }

    private Document findDocument(String collectionName, JsonDynamicModel persisted) {
        Mono<Document> foundPublisher = mongoOperations.getCollection(collectionName)
                .flatMap(collection -> {
                    Document document = new Document("_id", new ObjectId(persisted.getId().getInternalId()));
                    Publisher<Document> publisher = collection.find(document).first();
                    return Mono.from(publisher);
                });

        return foundPublisher.block();
    }
}
