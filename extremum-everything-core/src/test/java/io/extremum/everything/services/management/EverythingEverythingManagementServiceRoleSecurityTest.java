package io.extremum.everything.services.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.security.RoleSecurity;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class EverythingEverythingManagementServiceRoleSecurityTest {
    private static final boolean DO_NOT_EXPAND = false;

    @InjectMocks
    private RoleSecurityEverythingEverythingManagementService secureService;

    private final Descriptor descriptor = Descriptor.builder()
            .externalId("external-id")
            .internalId("internal-id")
            .modelType("SecuredEntity")
            .build();

    @Mock
    private EverythingEverythingManagementService insecureService;
    @Mock
    private RoleSecurity roleSecurity;

    private final ResponseDto responseDto = mock(ResponseDto.class);
    private final JsonPatch jsonPatch = new JsonPatch(Collections.emptyList());

    @Test
    void givenSecurityRolesAllowGetAnEntity_whenGettingIt_itShouldBeReturned() {
        when(insecureService.get(descriptor, DO_NOT_EXPAND)).thenReturn(responseDto);

        ResponseDto dto = secureService.get(descriptor, DO_NOT_EXPAND);
        assertThat(dto, is(sameInstance(responseDto)));
    }

    @Test
    void givenSecurityRolesDoNotAllowGetAnEntity_whenGettingIt_anExceptionShouldBeThrown() {
        doThrow(new ExtremumAccessDeniedException("Access denied"))
                .when(roleSecurity).checkGetAllowed(descriptor);

        try {
            secureService.get(descriptor, false);
            fail("An exception should be thrown");
        } catch (ExtremumAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenSecurityRolesAllowPatchAnEntity_whenPatchingIt_itShouldBePatched() {
        when(insecureService.patch(descriptor, jsonPatch, DO_NOT_EXPAND)).thenReturn(responseDto);

        ResponseDto dto = secureService.patch(descriptor, jsonPatch, DO_NOT_EXPAND);
        assertThat(dto, is(sameInstance(responseDto)));
    }

    @Test
    void givenSecurityRolesDoNotAllowPatchAnEntity_whenPatchingIt_anExceptionShouldBeThrown() {
        doThrow(new ExtremumAccessDeniedException("Access denied"))
                .when(roleSecurity).checkPatchAllowed(descriptor);

        try {
            secureService.patch(descriptor, jsonPatch, DO_NOT_EXPAND);
            fail("An exception should be thrown");
        } catch (ExtremumAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenSecurityRolesAllowRemoveAnEntity_whenRemovingIt_itShouldBeRemoved() {
        secureService.remove(descriptor);
    }

    @Test
    void givenSecurityRolesDoNotAllowRemoveAnEntity_whenRemovingIt_anExceptionShouldBeThrown() {
        doThrow(new ExtremumAccessDeniedException("Access denied"))
                .when(roleSecurity).checkRemovalAllowed(descriptor);

        try {
            secureService.remove(descriptor);
            fail("An exception should be thrown");
        } catch (ExtremumAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

}
