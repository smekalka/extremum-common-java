package io.extremum.dynamic.validator.exceptions;

import io.extremum.dynamic.validator.Violation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class DynamicModelValidationException extends Exception {
    private final Set<Violation> violations;

    @Override
    public String getMessage() {
        return this.toString();
    }

    @Override
    public String toString() {
        String violationsString = violations.stream()
                .map(Violation::getMessage)
                .map(m -> String.format("<%s>", m))
                .collect(Collectors.joining("; "));

        return "Violations: " + violationsString;
    }
}
