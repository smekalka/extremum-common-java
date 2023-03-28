package io.extremum.exampleproject.config;

import io.extremum.authentication.api.Roles;
import io.extremum.security.NullReactiveSecurityProvider;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Component
public class DummySecurityProvider extends NullReactiveSecurityProvider {
    @Override
    public Mono<Boolean> hasAnyOfRoles(String... roles) {
        return Mono.just((Arrays.asList(roles).contains(Roles.ANONYMOUS)));
    }

    @Override
    public Mono<Object> getPrincipal() {
        return Mono.just("user");
    }
}
