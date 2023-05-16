package io.extremum.sharedmodels.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class ObjectMetadata {
    @JsonProperty("@id")
    private String id;
    @JsonProperty("@uuid")
    private String uuid;
    @JsonProperty("@type")
    private String type;
    @JsonProperty("@created")
    private ZonedDateTime created;
    @JsonProperty("@updated")
    private ZonedDateTime updated;
    @JsonProperty("@version")
    private Integer version;
    private String key;
    private String contentType;
    private Float size;

    public ObjectMetadata() {
    }

    @JsonCreator
    public ObjectMetadata(
            @JsonProperty("@id") String id,
            @JsonProperty("@uuid") String uuid,
            @JsonProperty("@type") String type,
            @JsonProperty("@created") ZonedDateTime created,
            @JsonProperty("@updated") ZonedDateTime updated,
            @JsonProperty("@version") Integer version,
            @JsonProperty("key") String key,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("size") Float size
    ) {
        this.id = id;
        this.uuid = uuid;
        this.type = type;
        this.created = created;
        this.updated = updated;
        this.version = version;
        this.key = key;
        this.contentType = contentType;
        this.size = size;
    }

    @Override
    public String toString() {
        return "ObjectMetadata{" +
                "id='" + id + '\'' +
                ", uuid='" + uuid + '\'' +
                ", type='" + type + '\'' +
                ", created=" + created +
                ", updated=" + updated +
                ", version=" + version +
                ", key='" + key + '\'' +
                ", contentType='" + contentType + '\'' +
                ", size=" + size +
                '}';
    }
}
