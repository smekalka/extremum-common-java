package io.extremum.sharedmodels.content;

import io.extremum.sharedmodels.basic.StringOrMultilingual;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class Display implements Serializable {
    @JsonProperty("type")
    private Type type;

    @JsonProperty("stringValue")
    private String stringValue;

    @JsonProperty("caption")
    private StringOrMultilingual caption;

    @JsonProperty("icon")
    private Media icon;

    @JsonProperty("splash")
    private Media splash;

    public Display(String value) {
        type = Type.STRING;
        stringValue = value;
    }

    public Display(StringOrMultilingual caption, Media icon, Media splash) {
        type = Type.OBJECT;
        this.caption = caption;
        this.icon = icon;
        this.splash = splash;
    }

    public boolean isString() {
        return Type.STRING.equals(type);
    }

    public boolean isObject() {
        return Type.OBJECT.equals(type);
    }

    public enum Type {
        @JsonProperty("string")
        STRING,
        @JsonProperty("object")
        OBJECT
    }

    public enum FIELDS {
        caption, icon, splash
    }
}
