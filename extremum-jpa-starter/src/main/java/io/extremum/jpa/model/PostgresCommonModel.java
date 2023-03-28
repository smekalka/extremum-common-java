package io.extremum.jpa.model;

import io.extremum.common.model.PersistableCommonModel;
import io.extremum.jpa.service.lifecycle.JpaCommonModelLifecycleListener;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.time.ZonedDateTime;
import java.util.UUID;

@Setter
@EntityListeners({JpaCommonModelLifecycleListener.class, AuditingEntityListener.class})
@MappedSuperclass
public abstract class PostgresCommonModel extends PostgresBasicModel implements PersistableCommonModel<UUID> {
    @Getter(onMethod_ = {@CreatedDate})
    private ZonedDateTime created;
    @Getter(onMethod_ = {@LastModifiedDate})
    private ZonedDateTime modified;
    @Getter(onMethod_ = {@Version})
    private Long version;
    @Getter(onMethod_ = {@Transient})
    private Boolean deleted = false;
    @Getter(onMethod_ = {@CreatedBy})
    private String createdBy;
    @Getter(onMethod_ = {@LastModifiedBy})
    private String modifiedBy;
}
