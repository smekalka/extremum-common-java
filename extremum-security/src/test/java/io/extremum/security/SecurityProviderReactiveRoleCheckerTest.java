package io.extremum.security;

import io.extremum.authentication.api.ReactiveSecurityProvider;
import io.extremum.security.provider.SecurityProviderReactiveRoleChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class SecurityProviderReactiveRoleCheckerTest {
    @InjectMocks
    private SecurityProviderReactiveRoleChecker roleChecker;

    @Mock
    private ReactiveSecurityProvider securityProvider;

    @Test
    void givenSecurityProviderAllowsAccess_whenCheckingViaRoleChecker_thenItShouldAllow() {
        when(securityProvider.hasAnyOfRoles("role1", "role2"))
                .thenReturn(Mono.just(true));

        assertThat(roleChecker.currentUserHasOneRoleOf("role1", "role2").block(), is(true));
    }

    @Test
    void givenSecurityProviderDeniesAccess_whenCheckingViaRoleChecker_thenItShouldDeny() {
        when(securityProvider.hasAnyOfRoles("role3"))
                .thenReturn(Mono.just(false));

        assertThat(roleChecker.currentUserHasOneRoleOf("role3").block(), is(false));
    }
}