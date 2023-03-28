package io.extremum.sharedmodels.spacetime;

import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.dto.RequestDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import java.util.List;

@Getter
@Setter
@ToString
@DocumentationName("Location")
public class LocationRequestDto implements RequestDto {
    private String kind;
    private String status;
    private String slug;
    private String uri;
    private StringOrMultilingual name;
    private StringOrMultilingual description;
    private ComplexAddress address;
    @Valid
    private Coordinates coordinates;
    @Valid
    private List<Coordinates> boundary;
}
