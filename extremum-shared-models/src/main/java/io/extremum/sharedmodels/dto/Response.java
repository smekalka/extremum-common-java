package io.extremum.sharedmodels.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.extremum.sharedmodels.logging.LoggingConstants;
import io.extremum.sharedmodels.constant.HttpStatus;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

/**
 * Common response structure
 */
@Getter
public class Response {
    private static final Logger LOGGER = LoggerFactory.getLogger(Response.class);

    private final ResponseStatusEnum status;
    private final Integer code;
    private final ZonedDateTime timestamp;
    @JsonProperty("rqid")
    private final String requestId;
    private final String locale;
    private final List<Alert> alerts;
    private final Object result;
    @JsonProperty("paged")
    private final Pagination pagination;

    @JsonCreator
    private Response(
            @JsonProperty("status") ResponseStatusEnum status,
            @JsonProperty("code") Integer code,
            @JsonProperty("timestamp") ZonedDateTime timestamp,
            @JsonProperty("rqid") String requestId,
            @JsonProperty("locale") String locale,
            @JsonProperty("alerts") List<Alert> alerts,
            @JsonProperty("result") Object result,
            @JsonProperty("paged") Pagination pagination) {
        this.status = status;
        this.code = code;
        this.timestamp = timestamp;
        this.requestId = requestId;
        this.locale = locale;
        this.alerts = alerts == null ? null : new LinkedList<>(Collections.unmodifiableList(alerts));
        this.result = result;
        this.pagination = pagination;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Response response) {
        return new Builder(response);
    }

    public static Response ok(Object result) {
        return builder()
                .withOkStatus()
                .withResult(result)
                .withNowTimestamp()
                .build();
    }

    public static Response ok(Object result, Alert alert) {
        return ok(result, singletonList(alert));
    }

    public static Response ok(Object result, List<Alert> alerts) {
        return builder()
                .withOkStatus()
                .withAlerts(alerts)
                .withResult(result)
                .withNowTimestamp()
                .build();
    }

    public static Response ok(Collection<? extends Serializable> result, Pagination pagination) {
        return builder()
                .withOkStatus()
                .withResult(result, pagination)
                .withNowTimestamp()
                .build();
    }

    public static Response ok() {
        return builder().withOkStatus().build();
    }

    public static Response fail(Alert alert, int code) {
        return fail(singletonList(alert), code);
    }

    public static Response fail(Collection<Alert> alerts, int code) {
        return Response.builder()
                .withFailStatus(code)
                .withAlerts(alerts)
                .withNowTimestamp()
                .build();
    }

    public boolean hasAlerts() {
        return alerts != null && !alerts.isEmpty();
    }

    public Response withRequestId(String requestId) {
        return new Response(status, code, timestamp, requestId, locale, alerts, result, pagination);
    }

    @Override
    public String toString() {
        return "Response{" +
                "status=" + status +
                ", code=" + code +
                ", timestamp=" + timestamp +
                ", requestId='" + requestId + '\'' +
                ", locale='" + locale + '\'' +
                ", alerts=" + alerts +
                ", result=" + result +
                ", pagination=" + pagination +
                '}';
    }

    public static class Builder {

        private ResponseStatusEnum status;
        private Integer code;
        private Object result;
        private String locale;
        private List<Alert> alerts;
        private ZonedDateTime timestamp;
        private Pagination pagination;
        private String requestId;

        public Builder() {
        }

        public Builder(Response response) {
            this.status = response.status;
            this.code = response.code;
            this.result = response.result;
            this.locale = response.locale;
            this.alerts = response.alerts;
            this.timestamp = response.timestamp;
            this.pagination = response.pagination;
            this.requestId = response.requestId;
        }

        public Builder withOkStatus() {
            return withOkStatus(HttpStatus.OK.value());
        }

        public Builder withOkStatus(int code) {
            this.status = ResponseStatusEnum.OK;
            this.code = code;

            withNowTimestamp();

            return this;
        }

        public Builder withFailStatus(int code) {
            this.status = ResponseStatusEnum.FAIL;
            this.code = code;

            withNowTimestamp();

            return this;
        }

        public Builder withDoingStatus() {
            this.status = ResponseStatusEnum.DOING;
            this.code = HttpStatus.PROCESSING.value();

            withNowTimestamp();

            return this;
        }

        public Builder withWarningStatus(int code) {
            this.status = ResponseStatusEnum.WARNING;
            this.code = code;

            withNowTimestamp();

            return this;
        }


        public Builder withPagination(Pagination pagination) {
            this.pagination = pagination;
            return this;
        }


        public Builder withResult(Object result) {
            this.result = result;
            if (pagination == null && result instanceof Collections) {
                pagination = Pagination.singlePage(((Collection) result).size());
            }
            return this;
        }

        public Builder withResult(Collection<? extends Serializable> result, Pagination pagination) {
            this.result = result;
            this.pagination = pagination;
            return this;
        }

        public Builder withAlert(Alert alert) {
            withAlerts(singletonList(alert));

            return this;
        }

        public Builder withAlerts(Collection<Alert> alerts) {
            Objects.requireNonNull(alerts, "Alerts cannot be a null list");

            if (this.alerts == null) {
                this.alerts = new ArrayList<>();
            }

            this.alerts.addAll(alerts);

            return this;
        }

        public Builder withNowTimestamp() {
            timestamp = ZonedDateTime.now();
            return this;
        }

        public Builder withLocale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder withRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Response build() {
            requireNonNull(status, "Status can't be null");
            requireNonNull(code, "Code can't be null");

            return new Response(status, code, timestamp,
                    requestId != null ? requestId : tryToDetermineRequestId(),
                    (this.locale == null ? Locale.getDefault().toLanguageTag() : this.locale),
                    alerts, result, pagination);
        }

        private String tryToDetermineRequestId() {
            return MDC.get(LoggingConstants.REQUEST_ID_ATTRIBUTE_NAME);
        }
    }
}
