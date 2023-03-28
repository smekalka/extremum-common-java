package io.extremum.sharedmodels.fundamental;

import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.RequestDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@ToString
@DocumentationName("External")
public class ExternalRequestDto implements RequestDto {
    @NotNull
    @NotEmpty
    private String clue;

    @NotNull
    @NotEmpty
    private String system;

    @NotNull
    @NotEmpty
    private String qualifier;

    @NotNull
    @Size(min = 1)
    private Set<Descriptor> objects;
}
