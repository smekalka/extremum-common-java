package io.extremum.sharedmodels.signal;

import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString
@DocumentationName("Subscriber")
public class SubscriberResponseDto extends CommonResponseDto {
    public static final String MODEL_NAME = "Subscriber";

    private String name;
    private Set<String> signals;
    private String url;

    @Override
    public String getModel() {
        return MODEL_NAME;
    }
}
