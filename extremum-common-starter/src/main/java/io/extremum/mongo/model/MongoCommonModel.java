package io.extremum.mongo.model;

import io.extremum.common.model.PersistableCommonModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;

import java.time.ZonedDateTime;

@Getter
@Setter
public abstract class MongoCommonModel implements PersistableCommonModel<ObjectId> {
    @Transient
    protected Descriptor uuid;

    @Transient
    protected String iri;

    @Id
    protected ObjectId id;

    @CreatedDate
    protected ZonedDateTime created;

    @LastModifiedDate
    protected ZonedDateTime modified;

    @CreatedBy
    protected String createdBy;

    @LastModifiedBy
    protected String modifiedBy;

    @Version
    protected Long version;

    protected Boolean deleted = false;

    @Override
    public String getIri() {
        if (uuid != null) {
            return uuid.getIri() != null ? uuid.getIri() : this.iri;
        }

        return this.iri;
    }

    public void setDeleted(Boolean newDeleted) {
        if (newDeleted == null) {
            throw new IllegalArgumentException("deleted cannot be null");
        }

        this.deleted = newDeleted;
    }
}
