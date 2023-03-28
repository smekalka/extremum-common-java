package io.extremum.sharedmodels.fundamental;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldId;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldResource;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Setter
@ToString
@DocumentationName("Object")
@JsonldResource
public abstract class CommonResponseDto extends Element implements ResponseDto {
    /**
     * Date/time of object creation in ISO-8601 format (uuuu-MM-dd'T'HH:mm:ss.SSSSSSXXX)
     */
    private ZonedDateTime created;

    /**
     * Date/time of object's last modification in ISO-8601 format (uuuu-MM-dd'T'HH:mm:ss.SSSSSSXXX)
     */
    private ZonedDateTime modified;

    /**
     * The object's version
     */
    private Long version;

    /**
     * The IRI/UUID of the folding object.
     */
    private String folder;

    private String createdBy;

    private String modifiedBy;

    @Override
    @JsonProperty("@uuid")
    public Descriptor getId() {
        return id;
    }

    @JsonldId
    public String getIri() {
        return iri;
    }

    @Override
    @JsonProperty("@version")
    public Long getVersion() {
        return version;
    }

    @Override
    @JsonProperty("@created")
    public ZonedDateTime getCreated() {
        return created;
    }

    @Override
    @JsonProperty("@modified")
    public ZonedDateTime getModified() {
        return modified;
    }

    @Override
    @JsonProperty("@type")
    public String getModel() {
        return id.getModelType();
    }
}