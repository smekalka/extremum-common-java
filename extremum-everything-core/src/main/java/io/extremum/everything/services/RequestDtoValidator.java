package io.extremum.everything.services;

import io.extremum.sharedmodels.dto.RequestDto;

import javax.validation.ConstraintViolation;
import java.util.Set;

public interface RequestDtoValidator {
    /**
     * If request is valid, no exception will be thrown. Otherwise a RequestDtoValidationException will be throw
     *
     * @param request RequestDto which do validated
     * @return
     */
    <R extends RequestDto> Set<ConstraintViolation<R>> validate(R request);
}
