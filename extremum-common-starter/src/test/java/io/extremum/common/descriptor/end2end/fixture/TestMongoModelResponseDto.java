package io.extremum.common.descriptor.end2end.fixture;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class TestMongoModelResponseDto extends CommonResponseDto {

    private String number;

    @Override
    @JsonIgnore
    public String getModel() {
        return TestMongoModel.MODEL_NAME;
    }
}
