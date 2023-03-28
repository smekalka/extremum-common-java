package io.extremum.watch.end2end.fixture;

import io.extremum.sharedmodels.dto.RequestDto;
import lombok.Data;

/**
 * @author rpuch
 */
@Data
public class WatchedModelRequestDto implements RequestDto {
    private String name;
}
