package io.extremum.sharedmodels.personal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Language {
    private String languageTag;
    private Level level;

    public enum Level {
        @JsonProperty("beginner")
        BEGINNER,
        @JsonProperty("elementary")
        ELEMENTARY,
        @JsonProperty("intermediate")
        INTERMEDIATE,
        @JsonProperty("upper_intermediate")
        UPPER_INTERMEDIATE,
        @JsonProperty("advanced")
        ADVANCED,
        @JsonProperty("proficiency")
        PROFICIENCY
    }
}
