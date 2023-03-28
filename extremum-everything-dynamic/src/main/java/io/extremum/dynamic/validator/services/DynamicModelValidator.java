package io.extremum.dynamic.validator.services;

import io.atlassian.fugue.Try;
import io.extremum.dynamic.models.DynamicModel;
import io.extremum.dynamic.validator.ValidationContext;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import reactor.core.publisher.Mono;

public interface DynamicModelValidator<Model extends DynamicModel<?>> {
    /**
     * Mono cat contains a Try object with {@link DynamicModelValidationException}, {@link SchemaNotFoundException}
     * or with {@link ValidationContext} value
     */
    Mono<Try<ValidationContext>> validate(Model model);
}
