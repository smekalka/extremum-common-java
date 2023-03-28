package io.extremum.batch.utils;

import io.extremum.batch.model.BatchRequestDto;
import io.extremum.batch.model.ValidatedRequest;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Set;
import java.util.function.Function;

import static java.lang.String.join;
import static java.util.stream.Collectors.joining;

public class BatchValidation {
    public static Function<BatchRequestDto, Mono<ValidatedRequest>> validateRequest(Validator validator) {
        return requestDto -> Mono.fromCallable(() -> {
            Set<ConstraintViolation<BatchRequestDto>> violations = validator.validate(requestDto);
            if (!violations.isEmpty()) {
                String failedMsg = violations.stream()
                        .map(v -> join(" - ", v.getPropertyPath().toString(), v.getMessage()))
                        .collect(joining(","));
                return new ValidatedRequest(requestDto.getId(), new ValidationException(failedMsg));
            }
            return new ValidatedRequest(requestDto.getId(),requestDto);
        });
    }
}
