package io.extremum.watch.config;

import io.extremum.everything.reactive.config.ReactiveEverythingConfiguration;
import io.extremum.security.AllowAnyRoleChecker;
import io.extremum.security.AllowEverythingForDataAccess;
import io.extremum.security.AllowEverythingForRoleAccess;
import io.extremum.security.DataSecurity;
import io.extremum.security.PrincipalSource;
import io.extremum.security.RoleChecker;
import io.extremum.security.RoleSecurity;
import io.extremum.security.rules.provider.DummySecurityRuleProvider;
import io.extremum.security.rules.provider.SecurityRuleProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@EnableAutoConfiguration(exclude = {
        MongoReactiveDataAutoConfiguration.class,
        ReactiveEverythingConfiguration.class
})
@ComponentScan(value = "io.extremum.watch.end2end.fixture")
public class WatchTestConfiguration {
    @Bean
    public PrincipalSource principalSource() {
        return () -> Optional.of(() -> "Alex");
    }

    @Bean
    public DataSecurity everythingDataSecurity() {
        return new AllowEverythingForDataAccess();
    }

    @Bean
    public RoleChecker roleChecker() {
        return new AllowAnyRoleChecker();
    }

    @Bean
    public RoleSecurity roleSecurity() {
        return new AllowEverythingForRoleAccess();
    }

    @Bean
    public SecurityRuleProvider securityRuleProvider() {
        return new DummySecurityRuleProvider();
    }
}
