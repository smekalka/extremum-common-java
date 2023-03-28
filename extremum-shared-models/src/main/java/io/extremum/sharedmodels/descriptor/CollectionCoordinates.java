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

/**
 * @author rpuch
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class CollectionCoordinates implements Serializable {
    @Id
    @GeneratedValue
    private UUID id;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private OwnedCoordinates ownedCoordinates;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private FreeCoordinates freeCoordinates;

    public CollectionCoordinates(OwnedCoordinates ownedCoordinates) {
        this.ownedCoordinates = ownedCoordinates;
    }

    public CollectionCoordinates(FreeCoordinates freeCoordinates) {
        this.freeCoordinates = freeCoordinates;
    }
}