package descriptor;

import com.mongodb.client.model.Filters;
import config.DescriptorConfiguration;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.test.TestWithServices;
import io.extremum.descriptors.common.DescriptorsMongoDb;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static descriptor.DocumentByNameMatcher.*;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author rpuch
 */
@SpringBootTest(classes = DescriptorConfiguration.class)
class DescriptorMongoStorageTest extends TestWithServices {
    private static final String EXPECTED_DESCRIPTOR_COLLECTION = "descriptor-identifiers";

    @Autowired
    private DescriptorSaver descriptorSaver;
    @Autowired
    @DescriptorsMongoDb
    private MongoOperations mongoOperations;

    @Test
    void whenDescriptorIsStored_thenItShouldBeStoredInCollectionNamedDescriptorIdentifiers() {
        Descriptor descriptor = createAndSaveNewDescriptor();

        assertTrue(mongoOperations.collectionExists(EXPECTED_DESCRIPTOR_COLLECTION),
                "Expected collection does not exist");
        assertFalse(mongoOperations.collectionExists("descriptor"), "Wrong collection exists");

        Document document = findDescriptorDocument(descriptor);
        assertThat(document.get("_id"), is(descriptor.getExternalId()));
    }

    private Document findDescriptorDocument(Descriptor descriptor) {
        List<Document> documents = mongoOperations.getCollection(EXPECTED_DESCRIPTOR_COLLECTION)
                .find(Filters.eq("_id", descriptor.getExternalId()))
                .into(new ArrayList<>());

        assertThat(documents, hasSize(1));

        return documents.get(0);
    }

    private Descriptor createAndSaveNewDescriptor() {
        return descriptorSaver.createAndSave(new ObjectId().toString(), "Test",
                StandardStorageType.MONGO, "iri", new HashMap<>());
    }

    @Test
    void whenDescriptorIsStored_thenCreatedAndModifiedAndVersionShouldBeSet() {
        Descriptor descriptor = createAndSaveNewDescriptor();

        Document document = findDescriptorDocument(descriptor);

        assertThat(document.get("created"), is(instanceOf(Date.class)));
        assertThat(document.get("modified"), is(instanceOf(Date.class)));
        assertThat(document.getLong("version"), is(0L));
    }

    @Test
    void makeSureAnIndexIsCreatedForDescriptorInternalId() {
        List<Document> indices = getIndicesOnDescriptorsCollection();

        assertThat(indices, hasItem(havingName("internalId")));
    }

    @NotNull
    private List<Document> getIndicesOnDescriptorsCollection() {
        return mongoOperations.getCollection(EXPECTED_DESCRIPTOR_COLLECTION)
                .listIndexes()
                .into(new ArrayList<>());
    }

    @Test
    void whenDescriptorIsSaved_thenDataStorageTypeShouldBeRepresentedWithInternalValueAndNotWithEnumFieldName() {
        Descriptor descriptor = createAndSaveNewDescriptor();

        Document document = findDescriptorDocument(descriptor);

        assertThat(document.get("storageType"), is("mongo"));
    }

    @Test
    void makeSureAnIndexIsCreatedForDescriptorCollectionCoordinates() {
        List<Document> indices = getIndicesOnDescriptorsCollection();

        assertThat(indices, hasItem(havingName("collection.coordinatesString")));
    }

    @Test
    void whenDescriptorIsStored_thenReadinessShouldBeSet() {
        Descriptor descriptor = createAndSaveNewDescriptor();

        Document document = findDescriptorDocument(descriptor);

        assertThat(document.get("readiness"), is("ready"));
    }
}
