package io.extremum.jpa.model;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.jpa.service.lifecycle.JpaCommonModelLifecycleListener;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.UUID;

@Setter
@EntityListeners(JpaCommonModelLifecycleListener.class)
@MappedSuperclass
public abstract class PostgresBasicModel implements BasicModel<UUID> {
    @Getter(onMethod_ = {@Transient})
    private Descriptor uuid;

    @Getter(onMethod_ = {@Transient})
    private String iri;

    @Type(type="pg-uuid")
    private UUID id;

    @Id
    @Type(type="pg-uuid")
    public UUID getId() {
        return id;
    }

    @Override
    public String getIri() {
        if (uuid != null) {
            return uuid.getIri() != null ? uuid.getIri() : this.iri;
        }

        return this.iri;
    }
}
