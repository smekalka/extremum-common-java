package io.extremum.jpa.model;

import io.extremum.common.annotation.ModelName;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@ModelName("AdvancedTestJpaModel")
@Access(AccessType.FIELD)
public class AdvancedTestJpaModel extends PostgresCommonModel {

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private StringOrMultilingual name;

    private Integer size;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<NestedModel> nestedmodels = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private NestedModel nested;

}
