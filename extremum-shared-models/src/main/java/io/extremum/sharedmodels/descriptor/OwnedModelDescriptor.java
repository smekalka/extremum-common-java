package io.extremum.sharedmodels.descriptor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class OwnedModelDescriptor {
    @Id
    private String id;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private OwnedModelCoordinates coordinates;

    public OwnedModelDescriptor(OwnedModelCoordinates coordinates) {
        this.coordinates = coordinates;
        this.id = coordinates.getOwnedCoordinates().toCoordinatesString();
    }

    public enum FIELDS {
        coordinates, coordinatesString
    }
}