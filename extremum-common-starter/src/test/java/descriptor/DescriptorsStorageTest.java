package descriptor;

import config.DescriptorConfiguration;
import io.extremum.common.test.TestWithServices;
import io.extremum.descriptors.common.DescriptorsMongoDb;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = DescriptorConfiguration.class)
class DescriptorsStorageTest extends TestWithServices {
    @Autowired
    @DescriptorsMongoDb
    private MongoOperations descriptorsMongoOperations;

    @Test
    void noCollectionDescriptorCollectionShouldBeCreated() {
        assertFalse(descriptorsMongoOperations.collectionExists("collectionDescriptor"));
    }
}
