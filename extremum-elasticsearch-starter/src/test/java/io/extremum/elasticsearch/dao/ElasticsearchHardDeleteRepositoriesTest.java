package io.extremum.elasticsearch.dao;

import io.extremum.elasticsearch.TestWithServices;
import io.extremum.elasticsearch.model.HardDeleteElasticsearchModel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = RepositoryBasedElasticsearchDaoConfiguration.class)
class ElasticsearchHardDeleteRepositoriesTest extends TestWithServices {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private HardDeleteElasticsearchModelDao dao;

    @Test
    void testCreateModel() {
        HardDeleteElasticsearchModel model = new HardDeleteElasticsearchModel();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        HardDeleteElasticsearchModel createdModel = dao.save(model);
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

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<HardDeleteElasticsearchModel> results = dao.findByName(uniqueName);
        assertThat(results, hasSize(2));
    }

    @NotNull
    private List<HardDeleteElasticsearchModel> oneDeletedAndOneNonDeletedWithGivenName(String uniqueName) {
        HardDeleteElasticsearchModel notDeleted = new HardDeleteElasticsearchModel();
        notDeleted.setName(uniqueName);

        HardDeleteElasticsearchModel deleted = new HardDeleteElasticsearchModel();
        deleted.setName(uniqueName);
        deleted.setDeleted(true);

        return Arrays.asList(notDeleted, deleted);
    }

    @Test
    void testThatSpringDataMagicCounterMethodWorksAndIgnoresDeletedAttribute() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        assertThat(dao.countByName(uniqueName), is(2L));
    }
}
