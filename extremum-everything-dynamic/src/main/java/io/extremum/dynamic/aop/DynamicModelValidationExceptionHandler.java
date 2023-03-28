package io.extremum.dynamic.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.response.status.ResponseStatusCodeResolver;
import io.extremum.dynamic.validator.Violation;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestControllerAdvice
public class DynamicModelValidationExceptionHandler {

    private final ResponseStatusCodeResolver statusCodeResolver;

    public DynamicModelValidationExceptionHandler(ResponseStatusCodeResolver statusCodeResolver) {
        this.statusCodeResolver = statusCodeResolver;
    }

    @ExceptionHandler(DynamicModelValidationException.class)
    public Mono<ResponseEntity<Response>> handleDynamicModelValidationException(DynamicModelValidationException e,
            @RequestHeader HttpHeaders headers) {
        List<Alert> alerts = e.getViolations().stream()
                .map(Violation::getMessage)
                .map(Alert::errorAlert)
                .collect(toList());

        HttpStatus status = statusCodeResolver.getStatus(headers, HttpStatus.BAD_REQUEST);
        Response response = Response.fail(alerts, HttpStatus.BAD_REQUEST.value());
        ResponseEntity<Response> responseEntity = ResponseEntity
                .status(status)
                .body(response);
        return Mono.just(responseEntity);
    }
}
