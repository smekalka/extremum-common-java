package io.extremum.common.support;

import io.extremum.mongo.dao.MongoCommonDao;
import io.extremum.common.service.CommonService;
import io.extremum.mongo.service.impl.MongoCommonServiceImpl;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author rpuch
 */
class ListBasedCommonServicesTest {
    private static final MongoCommonDao<FirstModel> NOT_USED = null;

    private final FirstModelService firstModelService = new FirstModelService();
    private final CommonServices services = new ListBasedCommonServices(
            ImmutableList.of(firstModelService)
    );

    @Test
    void givenAServiceExists_whenGettingTheServiceByModelClass_thenItShouldBeFound() {
        CommonService<FirstModel> service = services.findServiceByModel(FirstModel.class);

        assertThat(service, is(notNullValue()));
        assertThat(service, is(sameInstance(firstModelService)));
    }

    @Test
    void givenAServiceDoesNotExist_whenGettingTheServiceByModelClass_thenAnExceptionShouldBeThrown() {
        try {
            services.findServiceByModel(SecondModel.class);
            fail("An exception should be thrown");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("Cannot find implementation of CommonService for model class "
                    + SecondModel.class.getName()));
        }
    }

    private static class FirstModelService extends MongoCommonServiceImpl<FirstModel> {
        FirstModelService() {
            super(NOT_USED);
        }
    }
}