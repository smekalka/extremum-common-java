package io.extremum.everything.aop;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.response.status.ResponseStatusCodeResolver;
import io.extremum.everything.controllers.EverythingExceptionHandlerTarget;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.everything.exceptions.RequestDtoValidationException;
import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import io.extremum.sharedmodels.descriptor.DescriptorNotReadyException;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static io.extremum.sharedmodels.dto.Alert.errorAlert;
import static io.extremum.sharedmodels.dto.Response.fail;


@RestControllerAdvice(annotations = EverythingExceptionHandlerTarget.class)
@Slf4j
public class DefaultEverythingEverythingExceptionHandler implements EverythingEverythingExceptionHandler {

    private final ResponseStatusCodeResolver statusCodeResolver;

    public DefaultEverythingEverythingExceptionHandler(ResponseStatusCodeResolver statusCodeResolver) {
        this.statusCodeResolver = statusCodeResolver;
    }

    @ExceptionHandler
    public ResponseEntity<Response> handleRequestDtoValidationException(RequestDtoValidationException e,
            @RequestHeader HttpHeaders headers) {
        Set<ConstraintViolation<RequestDto>> constraintsViolation = e.getConatraintsViolation();

        log.error("{} has occurred while validating a request DTO {}. Constraints violation: {}. " +
                        "The exception was caught in the DefaultEverythingEverythingExceptionHandler",
                e.getClass().getName(), e.getObject(), constraintsViolation);

        Response.Builder responseBuilder = Response.builder();

        for (ConstraintViolation<RequestDto> violation : constraintsViolation) {
            Alert alert = Alert.builder()
                    .withErrorLevel()
                    .withCode("400")
                    .withMessage(violation.getMessage() + ", you value: " + violation.getInvalidValue())
                    .withElement(violation.getPropertyPath().toString())
                    .build();

            responseBuilder.withAlert(alert);
        }

        HttpStatus statusCode = statusCodeResolver.getStatus(headers, HttpStatus.BAD_REQUEST);
        Response response = responseBuilder
                .withFailStatus(HttpStatus.BAD_REQUEST.value())
                .withNowTimestamp()
                .withResult("Unable to complete 'everything-everything' operation")
                .build();

        return ResponseEntity
                .status(statusCode)
                .body(response);
    }

    @ExceptionHandler
    public ResponseEntity<Response> handleEverythingEverythingException(EverythingEverythingException e,
            @RequestHeader HttpHeaders headers) {
        logException(e);

        HttpStatus status = statusCodeResolver.getStatus(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        Response response = fail(errorAlert(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity
                .status(status)
                .body(response);
    }

    private void logException(Exception e) {
        log.debug("Exception has occurred and will be handled in DefaultEverythingEverythingExceptionHandler: {}",
                e.getLocalizedMessage(), e);
    }

    @ExceptionHandler
    public ResponseEntity<Response> handleModelNotFoundException(ModelNotFoundException e,
            @RequestHeader HttpHeaders headers) {
        logException(e);

        HttpStatus status = statusCodeResolver.getStatus(headers, HttpStatus.NOT_FOUND);
        return ResponseEntity
                .status(status)
                .body(notFound());
    }

    private Response notFound() {
        return Response.builder()
                .withFailStatus(HttpStatus.NOT_FOUND.value())
                .withNowTimestamp()
                .build();
    }

    @ExceptionHandler
    public ResponseEntity<Response> handleDescriptorNotFoundException(DescriptorNotFoundException e,
            @RequestHeader HttpHeaders headers) {
        logException(e);

        HttpStatus status = statusCodeResolver.getStatus(headers, HttpStatus.NOT_FOUND);
        return ResponseEntity
                .status(status)
                .body(notFound());
    }

    @ExceptionHandler
    public ResponseEntity<Response> handleDescriptorNotReadyException(DescriptorNotReadyException e,
            @RequestHeader HttpHeaders headers) {
        logException(e);

        HttpStatus status = statusCodeResolver.getStatus(headers, HttpStatus.PROCESSING);
        Response response = Response.builder()
                .withDoingStatus()
                .withAlert(Alert.infoAlert("Requested entity is still being processed, please retry later"))
                .withNowTimestamp()
                .build();

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler
    public ResponseEntity<Response> handleEverythingAccessDeniedException(ExtremumAccessDeniedException e,
            @RequestHeader HttpHeaders headers) {
        log.debug("Exception has occurred and will be handled in DefaultEverythingEverythingExceptionHandler: {}",
                e.getLocalizedMessage(), e);

        HttpStatus status = statusCodeResolver.getStatus(headers, HttpStatus.FORBIDDEN);
        Response response = Response.builder()
                .withFailStatus(HttpStatus.FORBIDDEN.value())
                .withNowTimestamp()
                .build();

        return ResponseEntity
                .status(status)
                .body(response);
    }
}
