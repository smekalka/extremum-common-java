package io.extremum.watch.controller;

import io.extremum.common.response.status.ResponseStatusCodeResolver;
import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.watch.exception.WatchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice("io.extremum.watch.controller")
@ConditionalOnBean(WatchController.class)
public class WatchControllersExceptionHandler {

    private final ResponseStatusCodeResolver statusCodeResolver;

    public WatchControllersExceptionHandler(ResponseStatusCodeResolver statusCodeResolver) {
        this.statusCodeResolver = statusCodeResolver;
    }

    @ExceptionHandler
    public ResponseEntity<Response> handleWatchException(WatchException e, @RequestHeader HttpHeaders headers) {
        log.error("Exception has occurred and will be handled in WatchControllersExceptionHandler: {}",
                e.getLocalizedMessage(), e);

        HttpStatus status = statusCodeResolver.getStatus(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        Alert alert = Alert.errorAlert("Internal server error");
        Response response = Response.fail(alert, HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler
    public ResponseEntity<Response> handleEverythingAccessDeniedException(ExtremumAccessDeniedException e,
            @RequestHeader HttpHeaders headers) {
        log.debug("Exception has occurred and will be handled in WatchControllersExceptionHandler: {}",
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
