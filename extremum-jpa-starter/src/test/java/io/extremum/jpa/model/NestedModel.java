package io.extremum.jpa.model;

import io.extremum.common.annotation.ModelName;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

@Entity
@Setter
@Getter
@ModelName("InnerTestJpaModel")
@Access(AccessType.FIELD)
public final class NestedModel extends PostgresCommonModel {

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private StringOrMultilingual name;

    private Integer size;

}
