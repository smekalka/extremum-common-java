package io.extremum.elasticsearch.reactive;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.elasticsearch.TestWithServices;
import io.extremum.elasticsearch.dao.RepositoryBasedElasticsearchDaoConfiguration;
import io.extremum.elasticsearch.dao.TestElasticsearchModelDao;
import io.extremum.elasticsearch.model.TestElasticsearchModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = RepositoryBasedElasticsearchDaoConfiguration.class)
class ElasticsearchUniversalReactiveModelLoaderTest extends TestWithServices {
    @Autowired
    private ElasticsearchUniversalReactiveModelLoader loader;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TestElasticsearchModelDao dao;

    @Test
    void givenTestModelExists_whenLoadingItReactively_thenItShouldBeLoaded() {
        TestElasticsearchModel savedModel = dao.save(new TestElasticsearchModel());

        Mono<Model> mono = loader.loadByInternalId(savedModel.getId(), TestElasticsearchModel.class);
        TestElasticsearchModel retrievedModel = (TestElasticsearchModel) mono.block();

        assertThat(retrievedModel, is(notNullValue()));
        assertThat(retrievedModel.getId(), is(equalTo(savedModel.getId())));
    }

    @Test
    void givenTestModelIsDeleted_whenLoadingItReactively_thenItShouldNotBeFound() {
        TestElasticsearchModel savedModel = dao.save(new TestElasticsearchModel());
        dao.deleteById(savedModel.getId());

        Mono<Model> mono = loader.loadByInternalId(savedModel.getId(), TestElasticsearchModel.class);
        assertThat(mono.block(), is(nullValue()));
    }
}