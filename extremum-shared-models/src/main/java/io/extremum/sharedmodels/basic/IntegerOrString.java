package io.extremum.sharedmodels.basic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

import static lombok.AccessLevel.*;

@Getter
@Setter(PRIVATE)
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = PRIVATE)
public class IntegerOrString implements Serializable {
    @JsonProperty("type")
    private Type type;
    @JsonProperty("integerValue")
    private Integer integerValue;
    @JsonProperty("stringValue")
    private String stringValue;

    public IntegerOrString(int value) {
        type = Type.NUMBER;
        integerValue = value;
    }

    public IntegerOrString(String value) {
        type = Type.STRING;
        stringValue = value;
    }

    public boolean isInteger() {
        return Type.NUMBER.equals(type);
    }

    public boolean isString() {
        return Type.STRING.equals(type);
    }

    private enum Type {
        @JsonProperty("number")
        NUMBER,
        @JsonProperty("string")
        STRING
    }
}
