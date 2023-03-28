package io.extremum.jpa.model;

import io.extremum.sharedmodels.basic.NestedModel;
import org.hibernate.annotations.Type;

import javax.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
public class PostgresEmbeddedModel implements NestedModel {

    public UUID getNestedId() {
        return nestedId;
    }

    public void setNestedId(UUID nestedId) {
        this.nestedId = nestedId;
    }

    @Type(type = "pg-uuid")
    private UUID nestedId = UUID.randomUUID();

}
