package io.extremum.jpa.model;

import javax.persistence.*;
import java.util.UUID;

/**
 * @author rpuch
 */
@Entity
@Table(name = "child")
public class Child {
    @Id
    @GeneratedValue
    public UUID id;
    public String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    public Parent parent;
}
