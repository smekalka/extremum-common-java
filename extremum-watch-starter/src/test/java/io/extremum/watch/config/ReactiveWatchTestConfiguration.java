package io.extremum.watch.config;

import io.extremum.mongo.springdata.reactiverepository.EnableExtremumReactiveMongoRepositories;
import io.extremum.security.AllowEverythingForDataAccessReactively;
import io.extremum.security.AllowEverythingForRoleAccessReactively;
import io.extremum.security.ReactiveAllowAnyRoleChecker;
import io.extremum.security.ReactiveDataSecurity;
import io.extremum.security.ReactivePrincipalSource;
import io.extremum.security.ReactiveRoleChecker;
import io.extremum.security.ReactiveRoleSecurity;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
@EnableExtremumReactiveMongoRepositories(basePackages = "io.extremum.watch")
@EnableAutoConfiguration
@ComponentScan("io.extremum.watch.end2end.fixture")
public class ReactiveWatchTestConfiguration {
    @Bean
    public ReactivePrincipalSource reactivePrincipalSource() {
        return () -> Mono.just(()->"Alex");
    }

    @Bean
    @Primary
    public ReactiveDataSecurity reactiveEverythingDataSecurity() {
        return new AllowEverythingForDataAccessReactively();
    }

    @Bean
    public ReactiveRoleChecker reactiveRoleChecker() {
        return new ReactiveAllowAnyRoleChecker();
    }

    @Bean
    public ReactiveRoleSecurity reactiveRoleSecurity() {
        return new AllowEverythingForRoleAccessReactively();
    }
}
