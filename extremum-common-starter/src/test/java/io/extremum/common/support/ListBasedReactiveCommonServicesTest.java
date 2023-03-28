package io.extremum.common.support;

import io.extremum.common.iri.service.IriFacilities;
import io.extremum.common.service.ReactiveCommonService;
import io.extremum.mongo.dao.ReactiveMongoCommonDao;
import io.extremum.mongo.service.impl.ReactiveMongoCommonServiceImpl;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author rpuch
 */
class ListBasedReactiveCommonServicesTest {
    private static final ReactiveMongoCommonDao<FirstModel> NOT_USED = null;
    private static final IriFacilities NOT_USED_IRI_FACILITY = null;

    private final FirstModelService firstModelService = new FirstModelService();
    private final ReactiveCommonServices services = new ListBasedReactiveCommonServices(
            ImmutableList.of(firstModelService)
    );

    @Test
    void givenAServiceExists_whenGettingTheServiceByModelClass_thenItShouldBeFound() {
        ReactiveCommonService<FirstModel> service = services.findServiceByModel(FirstModel.class);

        assertThat(service, is(notNullValue()));
        assertThat(service, is(sameInstance(firstModelService)));
    }

    @Test
    void givenAServiceDoesNotExist_whenGettingTheServiceByModelClass_thenAnExceptionShouldBeThrown() {
        try {
            services.findServiceByModel(SecondModel.class);
            fail("An exception should be thrown");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("Cannot find implementation of ReactiveCommonService for model class "
                    + SecondModel.class.getName()));
        }
    }

    private static class FirstModelService extends ReactiveMongoCommonServiceImpl<FirstModel> {
        FirstModelService() {
            super(NOT_USED, NOT_USED_IRI_FACILITY);
        }
    }
}