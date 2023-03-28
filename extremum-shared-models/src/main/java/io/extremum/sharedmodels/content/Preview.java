package io.extremum.sharedmodels.content;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * The Preview of the object, which allows to give a textual and visual explanation about it without having its data fetched.
 */
@Data
public class Preview {
    /**
     * A plain text caption that represents the object.
     */
    @NotNull
    @NotEmpty
    private String caption;
    private Media icon;
    private Media splash;
}
