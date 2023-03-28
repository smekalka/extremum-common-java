package io.extremum.common.exceptions;


import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author vov4a, scalagrammer
 */
public class CommonException extends RuntimeException {

    private final List<Alert> alerts = new ArrayList<>();

    private final int code;

    public CommonException(String message, int status) {
        super(message);
        this.code = status;
        this.alerts.add(Alert.errorAlert(message, null, String.valueOf(status)));
    }

    public CommonException(String message, int status, Throwable cause) {
        super(message, cause);
        this.code = status;
        this.alerts.add(Alert.errorAlert(message, null, String.valueOf(status)));
    }

    public CommonException(Throwable cause, String message, int statusCode) {
        super(message, cause);
        this.code = statusCode;
        this.alerts.add(Alert.errorAlert(message, null, String.valueOf(statusCode)));
    }

    public CommonException(String message, int statusCode, Collection<Alert> alerts) {
        super(message);
        this.code = statusCode;
        this.alerts.addAll(alerts);
    }

    public CommonException(int code) {
        this.code = code;
    }

    public CommonException withAlerts(Collection<Alert> alerts) {
        this.alerts.addAll(alerts);
        return this;
    }

    public CommonException withAlert(Alert alert) {
        this.alerts.add(alert);
        return this;
    }

    public int getCode() {
        return code;
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public Alert getFirstAlert() {
        return getAlerts().get(0);
    }

    private Response toFailResponse(String locale) {
        return Response.builder()
                .withFailStatus(code)
                .withLocale(locale)
                .withAlerts(alerts)
                .build();
    }

    private Response toFailResponse() {
        return this.toFailResponse(null); // without locale
    }

    @Override
    public String toString() {
        return toFailResponse().toString();
    }
}
