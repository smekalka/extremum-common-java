package io.extremum.sharedmodels.descriptor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Entity
public final class CollectionDescriptor implements Serializable {
    private Type type;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private CollectionCoordinates coordinates;
    @Id
    String id;
    private String coordinatesString;

    public CollectionDescriptor() {
    }

    private CollectionDescriptor(Type type, CollectionCoordinates coordinates) {
        this.type = type;
        this.coordinates = coordinates;
        this.id = type.toCoordinatesString(coordinates);
    }

    public static CollectionDescriptor forOwned(Descriptor hostId, String hostAttributeName) {
        return new CollectionDescriptor(Type.OWNED,
                new CollectionCoordinates(new OwnedCoordinates(hostId, hostAttributeName)));
    }

    public static CollectionDescriptor forFree(String name) {
        return new CollectionDescriptor(Type.FREE,
                new CollectionCoordinates(new FreeCoordinates(name)));
    }

    @Override
    public String toString() {
        return coordinatesString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CollectionDescriptor that = (CollectionDescriptor) o;
        return type == that.type && Objects.equals(coordinates, that.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, coordinates);
    }

    public String toCoordinatesString() {
        return type.toCoordinatesString(coordinates);
    }

    public void refreshCoordinatesString() {
        coordinatesString = toCoordinatesString();
    }

    public enum Type {
        /**
         * Collection is identified by host (owning) entity and host field name.
         */
        @JsonProperty("owned")
        OWNED {
            @Override
            String toCoordinatesString(CollectionCoordinates coordinates) {
                return coordinates.getOwnedCoordinates().toCoordinatesString();
            }
        },
        /**
         * Collection is identified by name (and optional parameter string).
         */
        @JsonProperty("free")
        FREE {
            @Override
            String toCoordinatesString(CollectionCoordinates coordinates) {
                return coordinates.getFreeCoordinates().toCoordinatesString();
            }
        };

        abstract String toCoordinatesString(CollectionCoordinates coordinates);
    }

    public enum FIELDS {
        type, coordinates, coordinatesString
    }
}
