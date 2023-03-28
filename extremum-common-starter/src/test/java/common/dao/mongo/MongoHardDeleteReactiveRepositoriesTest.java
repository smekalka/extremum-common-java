package common.dao.mongo;

import io.extremum.common.test.TestWithServices;
import models.HardDeleteMongoModel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
class MongoHardDeleteReactiveRepositoriesTest extends TestWithServices {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private HardDeleteReactiveMongoDao dao;

    @Test
    void testCreateModel() {
        HardDeleteMongoModel model = new HardDeleteMongoModel();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        HardDeleteMongoModel createdModel = dao.save(model).block();
        assertEquals(model, createdModel);
        assertNotNull(model.getId());
        assertNotNull(model.getCreated());
        assertNotNull(model.getModified());
        assertNotNull(model.getVersion());
        assertFalse(model.getDeleted());
    }

    @Test
    void testThatSpringDataMagicQueryMethodWorksAndIgnoresDeletedAttribute() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        List<HardDeleteMongoModel> results = dao.findByName(uniqueName).toStream().collect(Collectors.toList());
        assertThat(results, hasSize(2));
    }

    @NotNull
    private List<HardDeleteMongoModel> oneDeletedAndOneNonDeletedWithGivenName(String uniqueName) {
        HardDeleteMongoModel notDeleted = new HardDeleteMongoModel();
        notDeleted.setName(uniqueName);

        HardDeleteMongoModel deleted = new HardDeleteMongoModel();
        deleted.setName(uniqueName);
        deleted.setDeleted(true);

        return Arrays.asList(notDeleted, deleted);
    }

    @Test
    void testThatSpringDataMagicCounterMethodWorksAndIgnoresDeletedAttribute() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        assertThat(dao.countByName(uniqueName).block(), is(2L));
    }
}
