package io.extremum.dynamic.everything.management;

import io.extremum.dynamic.DefaultReactiveDescriptorDeterminator;
import io.extremum.dynamic.DefaultSchemaMetaService;
import io.extremum.dynamic.ReactiveDescriptorDeterminator;
import io.extremum.dynamic.SchemaMetaService;
import io.extremum.dynamic.models.DynamicModel;
import io.extremum.everything.services.management.ReactiveEverythingManagementService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.just;

class HybridEverythingManagementServiceTest {
    static Descriptor DYNAMIC_MODEL_DESCRIPTOR;
    static Descriptor STANDARD_MODEL_DESCRIPTOR;

    static HybridEverythingManagementService service;

    ReactiveEverythingManagementService defaultModelEvrService;
    ReactiveDynamicModelEverythingManagementService dynModelEvrService;
    static SchemaMetaService schemaMetaService;
    static ReactiveDescriptorDeterminator reactiveDescriptorDeterminator;

    @BeforeAll
    static void beforeAll() {
        DYNAMIC_MODEL_DESCRIPTOR = Descriptor.builder()
                .internalId("i-id_d")
                .externalId("e-id_d")
                .modelType(DynamicModel.MODEL_TYPE)
                .build();

        STANDARD_MODEL_DESCRIPTOR = Descriptor.builder()
                .internalId("i-id_s")
                .externalId("e-id_s")
                .modelType("StandardModel")
                .build();

        schemaMetaService = new DefaultSchemaMetaService();
        reactiveDescriptorDeterminator = new DefaultReactiveDescriptorDeterminator(schemaMetaService);
        schemaMetaService.registerMapping(DYNAMIC_MODEL_DESCRIPTOR.getModelType(), "empty.schema.json", 1);
    }

    @BeforeEach
    void beforeEach() {
        defaultModelEvrService = mock(ReactiveEverythingManagementService.class);
        dynModelEvrService = mock(ReactiveDynamicModelEverythingManagementService.class);

        service = new HybridEverythingManagementService(defaultModelEvrService, dynModelEvrService, reactiveDescriptorDeterminator);

        doReturn(just(mock(ResponseDto.class)))
                .when(defaultModelEvrService).get(any(), anyBoolean());
        doReturn(just(mock(ResponseDto.class)))
                .when(defaultModelEvrService).patch(any(), any(), anyBoolean());
        doReturn(just(empty()))
                .when(defaultModelEvrService).remove(any());

        doReturn(just(mock(ResponseDto.class)))
                .when(dynModelEvrService).get(any(), anyBoolean());
        doReturn(just(mock(ResponseDto.class)))
                .when(dynModelEvrService).patch(any(), any(), anyBoolean());
        doReturn(just(empty()))
                .when(dynModelEvrService).remove(any());
    }

    @Test
    void callStandardEvrManagementService_when_standardModelDescriptorPassed_to_getMethod() {
        service.get(STANDARD_MODEL_DESCRIPTOR, false).block();

        verify(defaultModelEvrService).get(eq(STANDARD_MODEL_DESCRIPTOR), anyBoolean());
        verify(dynModelEvrService, never()).get(any(), anyBoolean());
    }

    @Test
    void callStandardEvrManagementService_when_standardModelDescriptorPassed_to_patchMethod() {
        service.patch(STANDARD_MODEL_DESCRIPTOR, null, false).block();

        verify(defaultModelEvrService).patch(eq(STANDARD_MODEL_DESCRIPTOR), any(), anyBoolean());
        verify(dynModelEvrService, never()).patch(any(), any(), anyBoolean());
    }

    @Test
    void callStandardEvrManagementService_when_standardModelDescriptorPassed_to_removeMethod() {
        service.remove(STANDARD_MODEL_DESCRIPTOR).block();

        verify(defaultModelEvrService).remove(eq(STANDARD_MODEL_DESCRIPTOR));
        verify(dynModelEvrService, never()).remove(any());
    }

    @Test
    void callDynamicEvrManagementService_when_dynamicModelDescriptorPassed_to_getMethod() {
        service.get(DYNAMIC_MODEL_DESCRIPTOR, false).block();

        verify(dynModelEvrService).get(eq(DYNAMIC_MODEL_DESCRIPTOR), anyBoolean());
        verify(defaultModelEvrService, never()).get(any(), anyBoolean());
    }

    @Test
    void callDynamicEvrManagementService_when_dynamicModelDescriptorPassed_to_patchMethod() {
        service.patch(DYNAMIC_MODEL_DESCRIPTOR, null, false).block();

        verify(dynModelEvrService).patch(eq(DYNAMIC_MODEL_DESCRIPTOR), any(), anyBoolean());
        verify(defaultModelEvrService, never()).patch(any(), any(), anyBoolean());
    }

    @Test
    void callDynamicEvrManagementService_when_dynamicModelDescriptorPassed_to_removeMethod() {
        service.remove(DYNAMIC_MODEL_DESCRIPTOR).block();

        verify(dynModelEvrService).remove(eq(DYNAMIC_MODEL_DESCRIPTOR));
        verify(defaultModelEvrService, never()).remove(any());
    }
}