package io.extremum.common.response.status;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class HeaderBasedResponseStatusCodeResolver implements ResponseStatusCodeResolver {

    public static final String STATUS_CODE = "Response-Code";
    public static final String ALWAYS_200 = "200";

    @Override
    public HttpStatus getStatus(HttpHeaders headers, HttpStatus status) {
        String value = headers.getFirst(STATUS_CODE);
        if (ALWAYS_200.equals(value)) {
            return HttpStatus.OK;
        }
        return status;
    }

}
