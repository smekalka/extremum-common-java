package io.extremum.common.exceptions.end2end.disabled.handler;

import io.extremum.common.exceptions.CommonException;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DisabledSpringExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Response> handle(CommonException e) {
        Response response = Response.builder()
                .withFailStatus(e.getCode() + 2)
                .withAlert(Alert.errorAlert("SpringExceptionHandler: " + e.getMessage()))
                .build();

        return ResponseEntity.status(e.getCode() + 2).body(response);
    }

}
