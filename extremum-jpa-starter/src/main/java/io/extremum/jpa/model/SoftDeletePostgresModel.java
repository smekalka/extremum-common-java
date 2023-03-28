package io.extremum.jpa.model;

import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@Setter
@MappedSuperclass
public abstract class SoftDeletePostgresModel extends PostgresCommonModel {
    @Override
    @Column
    public Boolean getDeleted() {
        return super.getDeleted();
    }
}
