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
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = RepositoryBasedElasticsearchDaoConfiguration.class)
class ElasticsearchHardDeleteReactiveRepositoriesTest extends TestWithServices {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private HardDeleteReactiveElasticsearchModelDao dao;

    @Test
    void testCreateModel() {
        HardDeleteElasticsearchModel model = new HardDeleteElasticsearchModel();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        HardDeleteElasticsearchModel createdModel = dao.save(model).block();
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

        List<HardDeleteElasticsearchModel> results = dao.findByName(uniqueName)
                .toStream().collect(Collectors.toList());
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

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        assertThat(dao.countByName(uniqueName).block(), is(2L));
    }

    @Test
    void givenAModelExists_whenItIsDeletedAndReturned_thenItShouldNotExistAnymore() {
        HardDeleteElasticsearchModel savedModel = dao.save(new HardDeleteElasticsearchModel()).block();

        dao.deleteByIdAndReturn(savedModel.getId()).block();

        assertThat(dao.existsById(savedModel.getId()).block(), is(false));
    }

    @Test
    void givenAModelExists_whenItIsDeletedAndReturned_thenTheReturnedEntityShouldBeTheSame() {
        HardDeleteElasticsearchModel savedModel = dao.save(new HardDeleteElasticsearchModel()).block();

        HardDeleteElasticsearchModel deletedModel = dao.deleteByIdAndReturn(savedModel.getId()).block();

        assertThat(deletedModel.getId(), is(equalTo(savedModel.getId())));
    }
}
