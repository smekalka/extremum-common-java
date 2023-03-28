package io.extremum.everything.services.defaultservices;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.CommonServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DefaultSaverImplTest {
    @InjectMocks
    private DefaultSaverImpl saver;

    @Spy
    private CommonServices commonServices = new ConstantCommonServices();

    @Mock
    private CommonService<TestModel> commonService;

    @Test
    void whenSavingAModel_thenItShouldBeSavedToItsCommonService() {
        TestModel model = new TestModel();
        
        saver.save(model);

        verify(commonService).save(model);
    }

    private static class TestModel extends MongoCommonModel {
    }

    private class ConstantCommonServices implements CommonServices {
        @SuppressWarnings("unchecked")
        @Override
        public <M extends Model> CommonService<M> findServiceByModel(Class<? extends M> modelClass) {
            return (CommonService<M>) commonService;
        }
    }
}