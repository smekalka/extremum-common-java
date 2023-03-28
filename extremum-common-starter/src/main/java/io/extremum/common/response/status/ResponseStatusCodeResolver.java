package io.extremum.common.response.status;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public interface ResponseStatusCodeResolver {
    HttpStatus getStatus(HttpHeaders headers, HttpStatus status);
}
