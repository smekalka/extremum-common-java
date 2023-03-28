package io.extremum.common.exceptions;

import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;
import org.springframework.http.HttpStatus;

public class ExceptionResponse {

    private final HttpStatus httpStatus;
    private final Response data;

    public ExceptionResponse(HttpStatus httpStatus, Response data) {
        this.httpStatus = httpStatus;
        this.data = data;
    }

    public static ExceptionResponse withMessageAndHttpStatus(String message, HttpStatus status) {
        Alert alert = Alert.errorAlert(message, null, Integer.toString(status.value()));
        Response response = Response.builder()
                .withFailStatus(status.value())
                .withAlert(alert)
                .build();
        return new ExceptionResponse(status, response);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Response getData() {
        return data;
    }

}
