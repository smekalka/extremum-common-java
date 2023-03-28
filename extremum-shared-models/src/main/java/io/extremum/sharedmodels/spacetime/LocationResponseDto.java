package io.extremum.sharedmodels.spacetime;

import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@DocumentationName("Location")
public class LocationResponseDto extends CommonResponseDto {
    public static final String MODEL_NAME = "Location";

    private String kind;
    private String status;
    private String slug;
    private String uri;
    private StringOrMultilingual name;
    private StringOrMultilingual description;
    private ComplexAddress address;
    private Coordinates coordinates;
    private List<Coordinates> boundary;

    @Override
    public String getModel() {
        return MODEL_NAME;
    }
}
