package io.extremum.common.support;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.common.service.CommonService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class UniversalModelFinderImplTest {
    @InjectMocks
    private UniversalModelFinderImpl modelFinder;

    @Mock
    private ModelClasses modelClasses;
    @Mock
    private CommonServices commonServices;

    private final Model1 model1 = new Model1();
    private final Model2 model2 = new Model2();
    @Mock
    private CommonService<Model1> service1;
    @Mock
    private CommonService<Model2> service2;

    @Test
    void whenFinding_thenModelsShouldBeFound() {
        doReturn(castToModelClass(Model1.class)).when(modelClasses).getClassByModelName("model1");
        doReturn(castToModelClass(Model2.class)).when(modelClasses).getClassByModelName("model2");
        doReturn(service1).when(commonServices).findServiceByModel(Model1.class);
        doReturn(service2).when(commonServices).findServiceByModel(Model2.class);
        when(service1.get("internal1")).thenReturn(model1);
        when(service2.get("internal2")).thenReturn(model2);

        List<Descriptor> descriptors = Arrays.asList(
                Descriptor.builder()
                        .externalId("external1")
                        .internalId("internal1")
                        .modelType("model1")
                        .storageType(StandardStorageType.MONGO)
                        .build(),
                Descriptor.builder()
                        .externalId("external2")
                        .internalId("internal2")
                        .modelType("model2")
                        .storageType(StandardStorageType.MONGO)
                        .build()
        );
        List<Model> models = modelFinder.findModels(descriptors);

        assertThat(models, is(equalTo(Arrays.asList(model1, model2))));
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private Class<Model> castToModelClass(Class<? extends Model> modelClass) {
        return (Class<Model>) modelClass;
    }

    private static class Model1 extends MongoCommonModel {
    }

    private static class Model2 extends MongoCommonModel {
    }
}