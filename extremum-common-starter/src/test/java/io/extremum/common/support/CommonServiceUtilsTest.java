package io.extremum.common.support;

import io.extremum.common.iri.properties.IriProperties;
import io.extremum.common.iri.service.DefaultIriFacilities;
import io.extremum.mongo.dao.MongoCommonDao;
import io.extremum.mongo.dao.ReactiveMongoCommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.mongo.service.impl.MongoCommonServiceImpl;
import io.extremum.mongo.service.impl.ReactiveMongoCommonServiceImpl;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class CommonServiceUtilsTest {
    private static final MongoCommonDao<A> NOT_USED = null;
    private static final ReactiveMongoCommonDao<A> REACTIVE_NOT_USED = null;

    private final AService aService = new AService(NOT_USED);
    private final AReactiveService aReactiveService = new AReactiveService(REACTIVE_NOT_USED);

    @Test
    void givenCommonServiceIsOfTypeA_whenFindCommonServiceModelClass_thenReturnClassA() {
        Class<?> modelClass = CommonServiceUtils.findCommonServiceModelClass(aService);
        assertThat(modelClass, is(sameInstance(A.class)));
    }

    @Test
    void givenReactiveCommonServiceIsOfTypeA_whenFindReactiveCommonServiceModelClass_thenReturnClassA() {
        Class<?> modelClass = CommonServiceUtils.findReactiveCommonServiceModelClass(aReactiveService);
        assertThat(modelClass, is(sameInstance(A.class)));
    }

    private static class A extends MongoCommonModel {
    }

    private static class AService extends MongoCommonServiceImpl<A> {
        AService(MongoCommonDao<A> dao) {
            super(dao);
        }
    }

    private static class AReactiveService extends ReactiveMongoCommonServiceImpl<A> {
        AReactiveService(ReactiveMongoCommonDao<A> dao) {
            super(dao, new DefaultIriFacilities(new IriProperties()));
        }
    }
}