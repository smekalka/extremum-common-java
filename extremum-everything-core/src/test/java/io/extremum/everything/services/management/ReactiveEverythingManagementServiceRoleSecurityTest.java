package io.extremum.everything.services.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.security.ReactiveRoleSecurity;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class ReactiveEverythingManagementServiceRoleSecurityTest {
    private static final boolean DO_NOT_EXPAND = false;

    @InjectMocks
    private RoleSecurityReactiveEverythingManagementService secureService;

    private final Descriptor descriptor = Descriptor.builder()
            .externalId("external-id")
            .internalId("internal-id")
            .modelType("SecuredEntity")
            .build();

    @Mock
    private ReactiveEverythingManagementService insecureService;
    @Mock
    private ReactiveRoleSecurity roleSecurity;

    private final ResponseDto responseDto = mock(ResponseDto.class);
    private final JsonPatch jsonPatch = new JsonPatch(Collections.emptyList());

    @BeforeEach
    void configureMocks() {
        lenient().when(insecureService.get(descriptor, DO_NOT_EXPAND))
                .thenReturn(Mono.just(responseDto));
        lenient().when(insecureService.patch(descriptor, jsonPatch, DO_NOT_EXPAND))
                .thenReturn(Mono.just(responseDto));
        lenient().when(insecureService.remove(any()))
                .thenReturn(Mono.empty());

        lenient().when(roleSecurity.checkGetAllowed(any())).thenReturn(Mono.empty());
        lenient().when(roleSecurity.checkPatchAllowed(any())).thenReturn(Mono.empty());
        lenient().when(roleSecurity.checkRemovalAllowed(any())).thenReturn(Mono.empty());
    }

    @Test
    void givenSecurityRolesAllowGetAnEntity_whenGettingIt_thenItShouldBeReturned() {
        ResponseDto dto = secureService.get(descriptor, DO_NOT_EXPAND).block();
        assertThat(dto, is(sameInstance(responseDto)));
    }

    @Test
    void givenSecurityRolesDoNotAllowGetAnEntity_whenGettingIt_thenAnExceptionShouldBeThrown() {
        when(roleSecurity.checkGetAllowed(descriptor))
                .thenReturn(Mono.error(new ExtremumAccessDeniedException("Access denied")));

        Mono<?> mono = secureService.get(descriptor, false);

        assertThatMonoThrowsAccessDeniedExceptionOnExecution(mono);
    }

    private void assertThatMonoThrowsAccessDeniedExceptionOnExecution(Mono<?> mono) {
        StepVerifier.create(mono)
                .expectErrorSatisfies(e -> assertAll(
                        () -> assertThat(e, instanceOf(ExtremumAccessDeniedException.class)),
                        () -> assertThat(e.getMessage(), is("Access denied"))
                ))
                .verify();
    }

    @Test
    void givenSecurityRolesAllowPatchAnEntity_whenPatchingIt_thenItShouldBePatched() {
        ResponseDto dto = secureService.patch(descriptor, jsonPatch, DO_NOT_EXPAND).block();
        assertThat(dto, is(sameInstance(responseDto)));
    }

    @Test
    void givenSecurityRolesDoNotAllowPatchAnEntity_whenPatchingIt_thenAnExceptionShouldBeThrown() {
        when(roleSecurity.checkPatchAllowed(descriptor))
                .thenReturn(Mono.error(new ExtremumAccessDeniedException("Access denied")));

        Mono<?> mono = secureService.patch(descriptor, jsonPatch, DO_NOT_EXPAND);

        assertThatMonoThrowsAccessDeniedExceptionOnExecution(mono);
    }

    @Test
    void givenSecurityRolesAllowRemoveAnEntity_whenRemovingIt_thenItShouldBeRemoved() {
        secureService.remove(descriptor).block();
    }

    @Test
    void givenSecurityRolesDoNotAllowRemoveAnEntity_whenRemovingIt_thenAnExceptionShouldBeThrown() {
        when(roleSecurity.checkRemovalAllowed(descriptor))
                .thenReturn(Mono.error(new ExtremumAccessDeniedException("Access denied")));

        Mono<?> mono = secureService.remove(descriptor);

        assertThatMonoThrowsAccessDeniedExceptionOnExecution(mono);
    }

}
