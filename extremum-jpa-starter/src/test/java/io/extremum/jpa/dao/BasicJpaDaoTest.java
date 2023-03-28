package io.extremum.jpa.dao;

import io.extremum.jpa.TestWithServices;
import io.extremum.jpa.model.TestBasicJpaModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = JpaCommonDaoConfiguration.class)
class BasicJpaDaoTest extends TestWithServices {
    @Autowired
    private TestBasicJpaModelDao dao;

    @Test
    void testCreateModel() {
        TestBasicJpaModel model = new TestBasicJpaModel();
        assertNull(model.getId());

        TestBasicJpaModel createdModel = dao.save(model);
        assertEquals(model, createdModel);
        assertNotNull(model.getId());
    }

    @Test
    void testCreateModelList() {
        int modelsToCreate = 10;
        List<TestBasicJpaModel> modelList = Stream
                .generate(TestBasicJpaModel::new)
                .limit(modelsToCreate)
                .collect(Collectors.toList());

        List<TestBasicJpaModel> createdModelList = dao.saveAll(modelList);
        assertNotNull(createdModelList);
        assertEquals(modelsToCreate, createdModelList.size());

        long validCreated = createdModelList.stream()
                .filter(model -> modelList.contains(model) && model.getId() != null)
                .count();
        assertEquals(modelsToCreate, validCreated);
    }

    @Test
    void testGet() {
        TestBasicJpaModel model = new TestBasicJpaModel();
        dao.save(model);

        TestBasicJpaModel resultModel = dao.findById(model.getId()).get();
        assertEquals(model.getId(), resultModel.getId());

        resultModel = dao.findById(UUID.randomUUID()).orElse(null);
        assertNull(resultModel);
    }

    @Test
    void testListAll() {
        int initCount = dao.findAll().size();
        int modelsToCreate = 10;

        for (int i = 0; i < modelsToCreate; i++) {
            dao.save(new TestBasicJpaModel());
        }
        int count = dao.findAll().size();
        assertEquals(initCount + modelsToCreate, count);
    }

    @Test
    void testDynamicQuery() {
        TestBasicJpaModel model = new TestBasicJpaModel();
        model.setName(UUID.randomUUID().toString());
        dao.save(model);

        List<TestBasicJpaModel> byName = dao.findByName(model.getName());
        assertThat(byName, hasSize(1));
    }
}
