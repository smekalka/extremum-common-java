package io.extremum.sharedmodels.descriptor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.UUID;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Setter
public class OwnedModelCoordinates {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private OwnedCoordinates ownedCoordinates;

    public OwnedModelCoordinates(OwnedCoordinates ownedCoordinates) {
        this.ownedCoordinates = ownedCoordinates;
    }
}
