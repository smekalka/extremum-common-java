package io.extremum.everything.services;

import io.extremum.sharedmodels.dto.RequestDto;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

@Slf4j
public class DefaultRequestDtoValidator implements RequestDtoValidator {
    @Override
    public <R extends RequestDto> Set<ConstraintViolation<R>> validate(R requestDto) {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        return validator.validate(requestDto);
    }
}
