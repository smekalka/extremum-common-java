package io.extremum.sharedmodels.personal;

import io.extremum.sharedmodels.basic.StringOrMultilingual;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Name {
    @JsonProperty("full")
    private StringOrMultilingual full;
    @JsonProperty("preferred")
    private StringOrMultilingual preferred;
    @JsonProperty("first")
    private StringOrMultilingual first;
    @JsonProperty("middle")
    private StringOrMultilingual middle;
    @JsonProperty("last")
    private StringOrMultilingual last;
    @JsonProperty("maiden")
    private StringOrMultilingual maiden;
    @JsonProperty("patronymic")
    private StringOrMultilingual patronymic;
    @JsonProperty("matronymic")
    private StringOrMultilingual matronymic;
}
