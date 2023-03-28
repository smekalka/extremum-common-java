package io.extremum.security;

import io.extremum.authentication.api.SecurityProvider;
import io.extremum.security.provider.SecurityProviderPrincipalSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class SecurityProviderPrincipalSourceTest {
    @InjectMocks
    private SecurityProviderPrincipalSource principalSource;

    @Mock
    private SecurityProvider securityProvider;

    @Test
    void givenPrincipalInSecurityProviderIsAlex_whenGettingPrincipal_thenAlexShouldBeReturned() {
        when(securityProvider.getPrincipal()).thenReturn(Optional.of(()->"Alex"));

        assertThat(principalSource.getPrincipal().get().getName(), is("Alex"));
    }

    @Test
    void givenPrincipalInSecurityProviderIs42_whenGettingPrincipal_then42StringShouldBeReturned() {
        when(securityProvider.getPrincipal()).thenReturn(Optional.of(()->"42"));

        assertThat(principalSource.getPrincipal().get().getName(), is("42"));
    }

    @Test
    void givenPrincipalInSecurityProviderIsNull_whenGettingPrincipal_thenNullShouldBeReturned() {
        when(securityProvider.getPrincipal()).thenReturn(Optional.empty());

        assertThat(principalSource.getPrincipal(), is(Optional.empty()));
    }
}