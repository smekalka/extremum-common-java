package io.extremum.sharedmodels.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@ToString
public class Alert {
    private AlertLevelEnum level;
    private String code;
    private ZonedDateTime timestamp;
    private String element;
    private String message;
    private String link;
    private String traceId;

    public static Alert errorAlert(String errorMessage) {
        return errorAlert(errorMessage, null, "400");
    }

    public static Alert errorAlert(String errorMessage, String element) {
        return errorAlert(errorMessage, element, "400");
    }

    public static Alert errorAlert(String errorMessage, String element, String code) {
        return Alert.builder()
                .withErrorLevel()
                .withElement(element)
                .withCode(code)
                .withMessage(errorMessage)
                .withNowTimestamp()
                .build();
    }

    public static Alert warningAlert(String message) {
        return warningAlert(message, null, "200");
    }

    public static Alert warningAlert(String message, String element) {
        return warningAlert(message, element, "200");
    }

    public static Alert warningAlert(String message, String element, String code) {
        return Alert.builder()
                .withWarningLevel()
                .withElement(element)
                .withCode(code)
                .withMessage(message)
                .withNowTimestamp()
                .build();
    }

    public static Alert infoAlert(String message) {
        return infoAlert(message, null, "200");
    }

    public static Alert infoAlert(String message, String element) {
        return infoAlert(message, message, "200");
    }

    public static Alert infoAlert(String message, String element, String code) {
        return Alert.builder()
                .withInfoLevel()
                .withElement(element)
                .withCode(code)
                .withMessage(message)
                .withNowTimestamp()
                .build();
    }

    @JsonIgnore
    public boolean isError() {
        return this.level == AlertLevelEnum.ERROR;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AlertLevelEnum level;
        private String message;
        private ZonedDateTime timestamp;
        private String element;
        private String code;
        private String link;
        private String traceId;

        public Builder withErrorLevel() {
            level = AlertLevelEnum.ERROR;
            return this;
        }

        public Builder withWarningLevel() {
            level = AlertLevelEnum.WARNING;
            return this;
        }

        public Builder withInfoLevel() {
            level = AlertLevelEnum.INFO;
            return this;
        }

        public Builder withMessage(String errorMessage) {
            message = errorMessage;
            return this;
        }

        public Builder withTimestamp(ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withNowTimestamp() {
            timestamp = ZonedDateTime.now();
            return this;
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withElement(String element) {
            this.element = element;
            return this;
        }

        public Builder withLink(String link) {
            this.link = link;
            return this;
        }

        public Builder withTraceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Alert build() {
            Alert alert = new Alert();

            alert.level = level;
            alert.message = message;
            alert.timestamp = timestamp;
            alert.code = code;
            alert.element = element;
            alert.link = link;
            alert.traceId = traceId;

            return alert;
        }
    }
}
