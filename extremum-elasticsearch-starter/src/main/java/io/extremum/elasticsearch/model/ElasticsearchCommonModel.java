package io.extremum.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.extremum.common.model.PersistableCommonModel;
import io.extremum.datetime.DateConstants;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.query.SeqNoPrimaryTerm;

import java.time.ZonedDateTime;

@Getter
@Setter
public abstract class ElasticsearchCommonModel implements PersistableCommonModel<String> {
    @Transient
    private Descriptor uuid;

    @Transient
    private String iri;

    @Field(name = "uuid", type = FieldType.Keyword)
    private String uuidString;
    @Id
    @Field(type = FieldType.Keyword)
    private String id;
    @CreatedDate
    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = DateConstants.DATETIME_FORMAT_WITH_MICROS)
    private ZonedDateTime created;
    @LastModifiedDate
    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = DateConstants.DATETIME_FORMAT_WITH_MICROS)
    private ZonedDateTime modified;
    @Version
    @Field(type = FieldType.Long)
    private Long version;
    @Field(type = FieldType.Boolean)
    private Boolean deleted = false;
    @JsonIgnore
    private SeqNoPrimaryTerm seqNoPrimaryTerm;

    @Override
    public Descriptor getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(Descriptor uuid) {
        this.uuid = uuid;
        uuidString = uuid == null ? null : uuid.getExternalId();
    }

    @Override
    public String getIri() {
        if (uuid != null) {
            return uuid.getIri() != null ? uuid.getIri() : this.iri;
        }

        return this.iri;
    }

    public String getUuidString() {
        return uuidString;
    }

    public void setUuidString(String uuidString) {
        this.uuidString = uuidString;
        uuid = uuidString == null ? null : new Descriptor(uuidString);
    }

    @Override
    public void copyServiceFieldsTo(Model to) {
        if (!(to instanceof ElasticsearchCommonModel)) {
            throw new IllegalStateException("I can only copy to an ElasticsearchCommonModel");
        }

        ElasticsearchCommonModel esTo = (ElasticsearchCommonModel) to;

        PersistableCommonModel.super.copyServiceFieldsTo(esTo);

        esTo.setSeqNoPrimaryTerm(this.getSeqNoPrimaryTerm());
    }
}
