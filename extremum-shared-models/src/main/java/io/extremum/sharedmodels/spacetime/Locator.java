package io.extremum.sharedmodels.spacetime;

import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DocumentationName("Locator")
public class Locator {
    private String code;
    private Type type;
    private StringOrMultilingual name;

    public enum Type {
        @JsonAlias("zip")
        @JsonProperty("postcode")
        POSTCODE,

        @JsonProperty("galaxy")
        GALAXY,

        @JsonAlias("pleiad")
        @JsonProperty("constellation")
        CONSTELLATION,

        @JsonAlias({"star", "comet", "asteroid"})
        @JsonProperty("planet")
        PLANET,

        @JsonProperty("continent")
        CONTINENT,

        @JsonAlias({"federation", "commonwealth", "empire"})
        @JsonProperty("union")
        UNION,

        @JsonAlias({"republic", "kingdom"})
        @JsonProperty("country")
        COUNTRY,

        @JsonProperty("state")
        STATE,

        @JsonAlias({"area", "district", "country", "province", "canton", "okrug", "oblast", "estate", "parish"})
        @JsonProperty("region")
        REGION,

        @JsonAlias("town")
        @JsonProperty("city")
        CITY,

        @JsonAlias({"road", "drive", "lane", "avenue"})
        @JsonProperty("street")
        STREET,

        @JsonAlias({"building", "terminal"})
        @JsonProperty("house")
        HOUSE,

        @JsonAlias("level")
        @JsonProperty("floor")
        FLOOR,

        @JsonAlias({"perron", "pier"})
        @JsonProperty("platform")
        PLATFORM,

        @JsonAlias({"sector", "lot"})
        @JsonProperty("section")
        SECTION,

        @JsonAlias({"hall", "office", "room", "compartment", "cabinet", "booth"})
        @JsonProperty("apartment")
        APARTMENT,

        @JsonAlias({"gate", "door", "porch"})
        @JsonProperty("entrance")
        ENTRANCE,

        @JsonAlias({"train", "voyage"})
        @JsonProperty("flight")
        FLIGHT,

        @JsonAlias({"carriage", "bus"})
        @JsonProperty("coach")
        COACH,

        @JsonAlias({"table", "balcony"})
        @JsonProperty("row")
        ROW,

        @JsonAlias({"seat", "stand", "berth", "bench", "shelf", "box", "cell"})
        @JsonProperty("place")
        PLACE,

        // airport iata code
        @JsonProperty("air")
        AIR
    }
}
