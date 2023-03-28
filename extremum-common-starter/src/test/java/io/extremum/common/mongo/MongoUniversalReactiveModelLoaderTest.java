package io.extremum.common.mongo;

import common.dao.mongo.MongoCommonDaoConfiguration;
import common.dao.mongo.TestMongoModelDao;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.test.TestWithServices;
import io.extremum.mongo.reactive.MongoUniversalReactiveModelLoader;
import models.TestMongoModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
class MongoUniversalReactiveModelLoaderTest extends TestWithServices {
    @Autowired
    private MongoUniversalReactiveModelLoader loader;
    @Autowired
    private TestMongoModelDao testMongoModelDao;

    @Test
    void givenTestModelExists_whenLoadingIt_thenItShouldBeFound() {
        TestMongoModel savedModel = testMongoModelDao.save(new TestMongoModel());

        Mono<Model> modelMono = loader.loadByInternalId(savedModel.getId().toString(), TestMongoModel.class);
        Model foundModel = modelMono.block();

        assertThat(foundModel, is(notNullValue()));
        assertThat(foundModel, is(instanceOf(TestMongoModel.class)));
        TestMongoModel foundTestModel = (TestMongoModel) foundModel;
        assertThat(foundTestModel.getId(), is(equalTo(savedModel.getId())));
    }
}