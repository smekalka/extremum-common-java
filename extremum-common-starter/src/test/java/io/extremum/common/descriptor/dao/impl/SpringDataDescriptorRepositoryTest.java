package io.extremum.common.descriptor.dao.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Updates;
import common.dao.mongo.MongoCommonDaoConfiguration;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.test.TestWithServices;
import io.extremum.descriptors.common.DescriptorsMongoDb;
import io.extremum.descriptors.common.dao.DescriptorRepository;
import io.extremum.descriptors.common.properties.DescriptorsMongoProperties;
import io.extremum.mongo.dbfactory.MainMongoDb;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author rpuch
 */
@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
@ImportAutoConfiguration(exclude = MongoReactiveRepositoriesAutoConfiguration.class)
class SpringDataDescriptorRepositoryTest extends TestWithServices {
    @Autowired
    private DescriptorRepository descriptorRepository;
    @Autowired
    private DescriptorService descriptorService;

    @Autowired
    @DescriptorsMongoDb
    private MongoOperations descriptorsMongoOperations;

    @Autowired
    @MainMongoDb
    private MongoClient mongoClient;
    @Autowired
    private DescriptorsMongoProperties descriptorsMongoProperties;

    @Test
    void whenDescriptorIsSaved_thenANewDocumentShouldAppearInMongo() {
        String internalId = new ObjectId().toString();
        Descriptor descriptor = newDescriptor(internalId);

        descriptorsMongoOperations.save(descriptor);

        List<Document> documents = mongoClient.getDatabase(descriptorsMongoProperties.getDescriptorsDbName())
                .getCollection("descriptor-identifiers")
                .find(eq("_id", descriptor.getExternalId()), Document.class)
                .into(new ArrayList<>());

        assertThatDocumentWith1DescriptorWasReturned(documents, descriptor);
    }

    private Descriptor newDescriptor(String internalId) {
        return Descriptor.builder()
                .externalId(descriptorService.createExternalId())
                .internalId(internalId)
                .modelType("test_model")
                .storageType(StandardStorageType.MONGO)
                .build();
    }

    private void assertThatDocumentWith1DescriptorWasReturned(List<Document> documents,
                                                              Descriptor expectedDescriptor) {
        assertThat(documents, hasSize(1));

        Document document = documents.get(0);

        assertThat(document.getString("_id"), is(equalTo(expectedDescriptor.getExternalId())));
        assertThat(document.getString("internalId"), is(equalTo(expectedDescriptor.getInternalId())));
        assertThat(document.getString("modelType"), is("test_model"));
        assertThat(document.getString("storageType"), is("mongo"));
        assertThat(document.get("created", Date.class), is(notNullValue()));
        assertThat(document.get("modified", Date.class), is(notNullValue()));
        assertThat(document.getBoolean("deleted"), is(false));
        assertThat(document.getLong("version"), is(0L));
    }

    @Test
    void whenDescriptorClassInMongoIsNotAvailable_thenDescriptorShouldBeLoadedSuccessfully() {
        String internalId = new ObjectId().toString();
        Descriptor descriptor = newDescriptor(internalId);

        descriptorsMongoOperations.save(descriptor);

        mongoClient.getDatabase(descriptorsMongoProperties.getDescriptorsDbName())
                .getCollection("descriptor-identifiers")
                .updateOne(
                        eq("_id", descriptor.getExternalId()),
                        Updates.set("_class", "no.such.class.AtAll")
                );

        Optional<Descriptor> retrievedDescriptorOpt = descriptorRepository.findByExternalId(descriptor.getExternalId());

        assertThatADescriptorWasFound(retrievedDescriptorOpt, descriptor);
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType"})
    private void assertThatADescriptorWasFound(Optional<Descriptor> retrievedDescriptorOpt,
                                               Descriptor expectedDescriptor) {
        assertThat(retrievedDescriptorOpt.isPresent(), is(true));

        Descriptor retrievedDescriptor = retrievedDescriptorOpt
                .orElseThrow(() -> new AssertionError("Retrieved nothing"));

        assertThat(retrievedDescriptor.getExternalId(), is(equalTo(expectedDescriptor.getExternalId())));
        assertThat(retrievedDescriptor.getInternalId(), is(equalTo(expectedDescriptor.getInternalId())));
    }
}