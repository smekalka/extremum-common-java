package io.extremum.jpa.dao;

import io.extremum.jpa.TestWithServices;
import io.extremum.jpa.model.HardDeleteJpaModel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = JpaCommonDaoConfiguration.class)
class JpaHardDeleteRepositoriesTest extends TestWithServices {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private HardDeleteJpaDao dao;

    @Test
    void testCreateModel() {
        HardDeleteJpaModel model = new HardDeleteJpaModel();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        HardDeleteJpaModel createdModel = dao.save(model);
        assertEquals(model, createdModel);
        assertNotNull(model.getId());
        assertNotNull(model.getCreated());
        assertNotNull(model.getModified());
        assertNotNull(model.getVersion());
        assertFalse(model.getDeleted());
    }

    @Test
    void testThatFindByIdWorksForAnEntityWithoutDeletedColumn() {
        HardDeleteJpaModel entity = dao.save(new HardDeleteJpaModel());
        Optional<HardDeleteJpaModel> opt = dao.findById(entity.getId());
        assertThat(opt.isPresent(), is(true));
    }

    @Test
    void testThatSpringDataMagicQueryMethodWorksAndIgnoresDeletedAttribute() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<HardDeleteJpaModel> results = dao.findByName(uniqueName);
        assertThat(results, hasSize(2));
    }

    @NotNull
    private List<HardDeleteJpaModel> oneDeletedAndOneNonDeletedWithGivenName(String uniqueName) {
        HardDeleteJpaModel notDeleted = new HardDeleteJpaModel();
        notDeleted.setName(uniqueName);

        HardDeleteJpaModel deleted = new HardDeleteJpaModel();
        deleted.setName(uniqueName);
        deleted.setDeleted(true);

        return Arrays.asList(notDeleted, deleted);
    }

    @Test
    void givenEntityExists_whenCallingDeleteById_thenItShouldNotBeFoundLater() {
        HardDeleteJpaModel model = dao.save(new HardDeleteJpaModel());

        dao.deleteById(model.getId());

        assertThat(dao.findById(model.getId()).isPresent(), is(false));
    }

    @Test
    void givenEntityExists_whenCallingDeleteByIdAndReturn_thenItShouldBeReturnedAndShouldNotBeFoundLater() {
        HardDeleteJpaModel model = dao.save(new HardDeleteJpaModel());

        HardDeleteJpaModel deletedModel = dao.deleteByIdAndReturn(model.getId());
        assertThat(deletedModel.getId(), is(equalTo(model.getId())));

        assertThat(dao.findById(model.getId()).isPresent(), is(false));
    }
}
