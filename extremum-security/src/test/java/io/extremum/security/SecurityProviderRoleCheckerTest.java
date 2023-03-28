package io.extremum.security;

import io.extremum.authentication.api.SecurityProvider;
import io.extremum.security.provider.SecurityProviderRoleChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class SecurityProviderRoleCheckerTest {
    @InjectMocks
    private SecurityProviderRoleChecker roleChecker;

    @Mock
    private SecurityProvider securityProvider;

    @Test
    void usesSecurityProviderToConsultRolesCurrentUserHas() {
        when(securityProvider.hasAnyOfRoles("role1", "role2")).thenReturn(true);

        assertThat(roleChecker.currentUserHasOneRoleOf("role1", "role2"), is(true));
        assertThat(roleChecker.currentUserHasOneRoleOf("role3"), is(false));
    }
}