package io.extremum.sharedmodels.descriptor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class FreeCoordinates implements Serializable {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String parametersString;

    public FreeCoordinates(String name) {
        this(name, null);
    }

    public FreeCoordinates(String name, String parametersString) {
        Objects.requireNonNull(name, "name cannot be null");
        if (name.contains("/")) {
            throw new IllegalArgumentException("name cannot contain /");
        }

        this.name = name;
        this.parametersString = parametersString;
    }

    public String toCoordinatesString() {
        return "FREE/" + name + (parametersString == null ? "" : "/" + parametersString);
    }
}
