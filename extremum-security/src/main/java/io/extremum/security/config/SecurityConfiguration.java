package io.extremum.security.config;

import io.extremum.authentication.api.SecurityProvider;
import io.extremum.security.auditor.SecuredAuditorAware;
import io.extremum.security.provider.ContextHolderSecurityProvider;
import io.extremum.sharedmodels.auth.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;

import java.security.Principal;
import java.util.Collections;
import java.util.Optional;

@Configuration
@Import(DataAccessCheckConfiguration.class)
public class SecurityConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Profile("secured")
    public SecurityProvider securityProvider() {
        return new ContextHolderSecurityProvider();
    }

    @Bean
    @Profile("secured")
    @ConditionalOnMissingBean
    public AuditorAware<String> auditorProvider() {
        return new SecuredAuditorAware();
    }

    @Bean("securityProvider")
    @ConditionalOnMissingBean
    @Profile("!secured")
    public SecurityProvider dummySecurityProvider() {
        return new SecurityProvider() {
            @Override
            public Optional<Principal> getPrincipal() {
                return Optional.of(new User("user", "", Collections.singletonList("user")));
            }

            @Override
            public boolean hasAnyOfRoles(String... roles) {
                return true;
            }

            @Override
            public <T> T getSessionExtension() {
                return null;
            }

            @Override
            public <T> T getIdentityExtension() {
                return null;
            }
        };
    }

    @Bean("auditorProvider")
    @Profile("!secured")
    @ConditionalOnMissingBean
    public AuditorAware<String> dummyAuditorProvider() {
        return () -> Optional.of("user");
    }
}
