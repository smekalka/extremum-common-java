package io.extremum.everything.services.management;

import com.google.common.collect.ImmutableList;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.everything.services.ReactiveGetterService;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.security.AllowEverythingForDataAccessReactively;
import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.security.ReactiveDataSecurity;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DefaultReactiveEverythingManagementServiceTest {
    private DefaultReactiveEverythingManagementService service;

    @Mock
    private DtoConversionService dtoConversionService;
    @Spy
    private ReactiveDataSecurity dataSecurity = new AllowEverythingForDataAccessReactively();
    @Mock
    private ReactiveDescriptorService reactiveDescriptorService;

    @BeforeEach
    void setUp() {
        service = new DefaultReactiveEverythingManagementService(
                new ModelRetriever(emptyList(),
                        ImmutableList.of(new AlwaysEmptyGetterService(), new AlwaysExistingGetterService()),
                        null, null,  new ModelNames(null)),
                null, null, null,
                dtoConversionService, dataSecurity, new ModelNames(null)
        );
    }

    @Test
    void givenGetterServiceReturnsNull_whenGetting_thenModelNotFoundExceptionShouldBeThrown() {
        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .internalId("internal-id")
                .modelType("AlwaysNull")
                .build();
        try {
            service.get(descriptor, false).block();
            fail("A ModelNotFoundException is expected");
        } catch (ModelNotFoundException e) {
            assertThat(e.getMessage(), is("Nothing was found by 'external-id'"));
        }
    }

    @Test
    void dataSecurityGetsConsultedOnAccessWhenGetting() {
        when(dataSecurity.checkGetAllowed(any()))
                .thenReturn(Mono.error(new ExtremumAccessDeniedException("Denied!")));
        lenient().when(dtoConversionService.convertUnknownToResponseDtoReactively(any(), any()))
                .thenReturn(Mono.just(mock(ResponseDto.class)));

        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .internalId("internal-id")
                .modelType("AlwaysExisting")
                .build();

        service.get(descriptor, false)
                .as(StepVerifier::create)
                .expectErrorSatisfies(ex -> {
                    assertThat(ex, instanceOf(ExtremumAccessDeniedException.class));
                    assertThat(ex.getMessage(), is("Denied!"));
                })
                .verify();
    }

    private static class AlwaysEmptyGetterService implements ReactiveGetterService<Model> {
        @Override
        public Mono<Model> get(String id) {
            return Mono.empty();
        }

        @Override
        public String getSupportedModel() {
            return "AlwaysNull";
        }
    }

    private static class AlwaysExistingModel extends MongoCommonModel {
    }

    private static class AlwaysExistingGetterService implements ReactiveGetterService<AlwaysExistingModel> {
        @Override
        public Mono<AlwaysExistingModel> get(String id) {
            return Mono.just(new AlwaysExistingModel());
        }

        @Override
        public String getSupportedModel() {
            return "AlwaysExisting";
        }
    }
}