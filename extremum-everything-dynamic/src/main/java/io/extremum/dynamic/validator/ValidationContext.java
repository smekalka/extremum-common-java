package io.extremum.dynamic.validator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public class ValidationContext {
    private final Set<String> paths;
}
