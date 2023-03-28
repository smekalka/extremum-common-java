package io.extremum.sharedmodels.descriptor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.util.UUID;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class OwnedCoordinates implements Serializable {
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Descriptor hostId;
    private String hostAttributeName;
    private String coordinatesString;
    private Class<?> hostAttributeClass;

    @Id
    @GeneratedValue
    private UUID id;

    public OwnedCoordinates(Descriptor hostId, String hostAttributeName) {
        this.hostId = hostId;
        this.hostAttributeName = hostAttributeName;
        this.coordinatesString = "OWNED/" + hostId.getExternalId() + "/" + hostAttributeName;
    }

    public String toCoordinatesString() {
        return coordinatesString;
    }
}
