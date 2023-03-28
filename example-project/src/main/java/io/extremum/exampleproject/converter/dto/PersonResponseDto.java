package io.extremum.exampleproject.converter.dto;

import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PersonResponseDto extends CommonResponseDto implements RequestDto {
    private String name;
    private String slug;
    private String field;
    private String address;
}
