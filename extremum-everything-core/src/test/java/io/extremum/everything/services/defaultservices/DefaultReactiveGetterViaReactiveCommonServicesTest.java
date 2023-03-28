package io.extremum.everything.services.defaultservices;

import io.extremum.common.service.ReactiveCommonService;
import io.extremum.common.support.ReactiveCommonServices;
import io.extremum.everything.support.ReactiveModelDescriptors;
import io.extremum.sharedmodels.basic.Model;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultReactiveGetterViaReactiveCommonServicesTest {
    @InjectMocks
    private DefaultReactiveGetterViaReactiveCommonServices getter;

    @Mock
    private ReactiveCommonServices commonServices;
    @Mock
    private ReactiveModelDescriptors modelDescriptors;

    @Mock
    private ReactiveCommonService<TestModel> commonService;

    private final TestModel modelFromDatabase = new TestModel();

    @Test
    void whenGetting_thenTheResultIsObtainedViaCommonService() {
        when(modelDescriptors.getModelClassByModelInternalId("internalId"))
                .thenReturn(Mono.just(modelClass(TestModel.class)));
        when(commonServices.findServiceByModel(TestModel.class)).thenReturn(commonService);
        when(commonService.get("internalId")).thenReturn(Mono.just(modelFromDatabase));

        Model model = getter.get("internalId").block();

        assertThat(model, is(sameInstance(modelFromDatabase)));
    }

    @SuppressWarnings("SameParameterValue")
    private Class<Model> modelClass(Class<? extends Model> modelClass) {
        @SuppressWarnings("unchecked") Class<Model> castClass = (Class<Model>) modelClass;
        return castClass;
    }

    private static class TestModel implements Model {
    }
}