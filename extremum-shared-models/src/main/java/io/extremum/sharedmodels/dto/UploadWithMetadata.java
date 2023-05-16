package io.extremum.sharedmodels.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class UploadWithMetadata {
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

    public UploadWithMetadata() {
    }

    @JsonCreator
    public UploadWithMetadata(
            @JsonProperty("@id") String id,
            @JsonProperty("@uuid") String uuid,
            @JsonProperty("@type") String type,
            @JsonProperty("@created") ZonedDateTime created,
            @JsonProperty("@updated") ZonedDateTime updated,
            @JsonProperty("@version") Integer version,
            @JsonProperty("key") String key
    ) {
        this.id = id;
        this.uuid = uuid;
        this.type = type;
        this.created = created;
        this.updated = updated;
        this.version = version;
        this.key = key;
    }

    @Override
    public String toString() {
        return "UploadWithMetadata{" +
                "id='" + id + '\'' +
                ", uuid='" + uuid + '\'' +
                ", type='" + type + '\'' +
                ", created=" + created +
                ", updated=" + updated +
                ", version=" + version +
                ", key='" + key + '\'' +
                '}';
    }
}
