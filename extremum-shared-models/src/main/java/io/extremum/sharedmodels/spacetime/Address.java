package io.extremum.sharedmodels.spacetime;

import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@ToString
@DocumentationName("Address")
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    @NotNull
    @JsonProperty("name")
    private StringOrMultilingual name;

    @NotNull
    @JsonProperty("locality")
    private List<Locator> locality;

    public enum FIELDS {
        string, locality
    }
}
