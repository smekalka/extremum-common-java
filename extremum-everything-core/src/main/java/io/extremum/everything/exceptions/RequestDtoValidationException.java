package io.extremum.everything.exceptions;

import io.extremum.sharedmodels.dto.RequestDto;
import lombok.Getter;
import lombok.Setter;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static java.lang.String.format;

@Getter
@Setter
public class RequestDtoValidationException extends EverythingEverythingException {
    private final RequestDto object;
    private final Set<ConstraintViolation<RequestDto>> conatraintsViolation;

    public RequestDtoValidationException(RequestDto dto, Set<ConstraintViolation<RequestDto>> constrainsViolation) {
        super(format("Constraints violation detected when validate an object %s", dto));
        this.object = dto;
        this.conatraintsViolation = constrainsViolation;
    }
}
