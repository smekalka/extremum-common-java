package io.extremum.sharedmodels.personal;

import io.extremum.sharedmodels.annotation.DocumentationName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "value")
@DocumentationName("Credential")
public class Credential {
    @NotNull
    @JsonProperty("system")
    private String system;

    @NotNull
    @JsonProperty("name")
    private VerifyType type;

    @NotNull
    @JsonProperty("value")
    private String value;
}
