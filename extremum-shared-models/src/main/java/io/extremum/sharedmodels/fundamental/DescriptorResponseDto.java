package io.extremum.sharedmodels.fundamental;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.content.Preview;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldNamespace;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldResource;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;

/**
 * An object descriptor represents some Object referenced by an attribute in another Object. The descriptor has the
 * id and url that allow to fetch the referenced object, plus it also contains display attributes that allow to build
 * an object preview without having its data fetched.
 * <p>
 * The properties section allows to give a partial extract of the referenced data object.
 * <p>
 * The ViewDescriptor may also refer to an Object, which doesn't exist, so it doesn't have id and url. In that case the
 * data given in properties is used to create a new object.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@DocumentationName("Descriptor")
@JsonldNamespace(name = "s", uri = "http://extremum.io/")
@JsonPropertyOrder(alphabetic = true)
@JsonldType("s:Descriptor")
@JsonldResource
public class DescriptorResponseDto<T> extends Mention {
    /**
     * The object's version number.Z
     */
    @JsonProperty("@version")
    private Long version;

    /**
     * The model that specifies the structure of the object referenced and described by the Descriptor.
     */
    @JsonProperty("@type")
    private String model;

    /**
     * Timestamp of the Descriptor in ISO-8601 format uuuu-MM-dd'T'hh:mm:ss.SSSSSSXXX.
     */
    private ZonedDateTime timestamp;

    /**
     * Some descriptive information that represents the object referenced and described by the ViewDescriptor.
     * May be a string or {@link Preview} object
     */
    private Object display;

    /**
     * Some or all properties of the described object.
     */
    private T properties;
}
