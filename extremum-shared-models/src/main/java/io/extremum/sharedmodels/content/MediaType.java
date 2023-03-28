package io.extremum.sharedmodels.content;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MediaType {
    TEXT("text"),
    IMAGE("image"),
    AUDIO("audio"),
    VIDEO("video"),
    APPLICATION("application"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_GIF("image/gif"),
    IMAGE_PNG("image/png");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }

    @JsonCreator
    public static MediaType fromString(String value) {
        if (value != null) {
            for (MediaType mediaType : values()) {
                if (value.equalsIgnoreCase(mediaType.value)) {
                    return mediaType;
                }
            }
        }

        return null;
    }
}
