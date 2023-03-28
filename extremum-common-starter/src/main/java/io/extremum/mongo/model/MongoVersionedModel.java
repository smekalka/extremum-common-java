package io.extremum.mongo.model;

import io.extremum.common.model.VersionedModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.ZonedDateTime;

@Getter
@Setter
@CompoundIndexes({
        @CompoundIndex(def = "{'lineageId': 1, 'start': 1, 'end': 1}", name = "lineageId_start_end"),
        @CompoundIndex(def = "{'lineageId': 1, 'currentSnapshot': 1, 'deleted': 1}",
                name = "lineageId_currentSnapshot_deleted"),
        @CompoundIndex(def = "{'lineageId': 1, 'version': 1}", unique = true,
                name = MongoVersionedModel.INDEX_BY_LINEAGEID_VERSION)
})
public abstract class MongoVersionedModel implements VersionedModel<ObjectId> {
    public static final String INDEX_BY_LINEAGEID_VERSION = "lineageId_version";

    @Transient
    private Descriptor uuid;

    @Id
    private ObjectId snapshotId;

    private String iri;

    @Indexed
    private ObjectId lineageId;

    @Indexed
    private ZonedDateTime created;
    private ZonedDateTime start;
    private ZonedDateTime end;
    @Indexed
    private boolean currentSnapshot;

    private Long version;

    private Boolean deleted = false;

    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String modifiedBy;

    @Override
    public ObjectId getId() {
        return getLineageId();
    }

    @Override
    public void setId(ObjectId id) {
        setLineageId(id);
    }

    public void setDeleted(Boolean newDeleted) {
        if (newDeleted == null) {
            throw new IllegalArgumentException("deleted cannot be null");
        }

        this.deleted = newDeleted;
    }
}
