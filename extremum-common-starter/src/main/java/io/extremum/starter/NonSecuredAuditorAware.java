package io.extremum.starter;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class NonSecuredAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.empty();
    }
}