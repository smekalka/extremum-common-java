package io.extremum.jpa.model;

import io.extremum.common.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "hard_deletable")
@Setter
@ModelName("HardDeletable")
public class HardDeleteJpaModel extends PostgresCommonModel {

    @Getter(onMethod_ = {@Column})
    private String name;

    public enum FIELDS {
        name
    }
}
