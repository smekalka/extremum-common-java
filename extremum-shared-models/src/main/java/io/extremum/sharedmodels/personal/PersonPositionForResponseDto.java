package io.extremum.sharedmodels.personal;

import io.extremum.sharedmodels.basic.IdOrObject;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.spacetime.LocationResponseDto;
import io.extremum.sharedmodels.spacetime.TimeFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author rpuch
 */
@Getter
@Setter
@ToString
public class PersonPositionForResponseDto {
    private StringOrMultilingual company;
    private StringOrMultilingual title;
    private StringOrMultilingual description;
    private TimeFrame timeframe;
    private IdOrObject<String, LocationResponseDto> location;
}
