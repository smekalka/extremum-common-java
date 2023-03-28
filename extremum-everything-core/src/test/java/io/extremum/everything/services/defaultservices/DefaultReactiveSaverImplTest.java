package io.extremum.everything.services.defaultservices;

import io.extremum.common.service.ReactiveCommonService;
import io.extremum.common.support.ReactiveCommonServices;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.sharedmodels.basic.Model;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.extremum.test.mockito.ReturnFirstArgInMono.returnFirstArgInMono;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DefaultReactiveSaverImplTest {
    @InjectMocks
    private DefaultReactiveSaverImpl saver;

    @Spy
    private ReactiveCommonServices commonServices = new ConstantReactiveCommonServices();

    @Mock
    private ReactiveCommonService<TestModel> commonService;

    @Test
    void whenSavingAModel_thenItShouldBeSavedToItsCommonService() {
        when(commonService.save(any())).then(returnFirstArgInMono());
        TestModel model = new TestModel();
        
        saver.save(model).block();

        //noinspection UnassignedFluxMonoInstance
        verify(commonService).save(model);
    }

    private static class TestModel extends MongoCommonModel {
    }

    private class ConstantReactiveCommonServices implements ReactiveCommonServices {
        @SuppressWarnings("unchecked")
        @Override
        public <M extends Model> ReactiveCommonService<M> findServiceByModel(Class<? extends M> modelClass) {
            return (ReactiveCommonService<M>) commonService;
        }
    }
}