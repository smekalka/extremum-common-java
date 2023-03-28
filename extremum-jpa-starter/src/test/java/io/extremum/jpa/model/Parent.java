package io.extremum.jpa.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author rpuch
 */
@Entity
@Table(name = "parent")
public class Parent {
    @Id
    @GeneratedValue
    public UUID id;
    public String name;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    public List<Child> children = new ArrayList<>();

    public String getName() {
        return name;
    }

    public List<Child> getChildren() {
        return children;
    }
}
