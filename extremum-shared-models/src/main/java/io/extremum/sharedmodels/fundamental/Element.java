package io.extremum.sharedmodels.fundamental;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.descriptor.Descriptor;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldId;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldNamespace;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldResource;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
import lombok.Setter;

@DocumentationName("Element")
@JsonPropertyOrder(alphabetic = true)
@JsonldResource
@Setter
public abstract class Element {
    /**
     * The unique IRI of the element.
     */
    @JsonldId
    public String iri;

    /**
     * The unique ID of the object
     */
    @JsonProperty("@uuid")
    public Descriptor id;
}
