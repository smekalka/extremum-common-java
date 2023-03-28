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

import java.net.URI;

@DocumentationName("Mention")
@JsonldNamespace(name = "s", uri = "http://extremum.io/")
@JsonPropertyOrder(alphabetic = true)
@JsonldType("s:Mention")
@JsonldResource
@Setter
public abstract class Mention {
    /**
     * The unique IRI of the mentioned element, which can be used for fetching its data.
     */
    @JsonldId
    protected URI iri;

    /**
     * The UUID of the mentioned element, which can be used for fetching its data.
     */
    @JsonProperty("@uuid")
    protected Descriptor id;
}
