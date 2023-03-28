package io.extremum.jpa.model;

import io.extremum.common.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "test_model")
@Where(clause = "deleted = false")
@Setter
@ModelName("TestJpaModel")
public class TestJpaModel extends SoftDeletePostgresModel {

    @Getter(onMethod_ = {@Column})
    private String name;

    public enum FIELDS {
        name
    }
}
