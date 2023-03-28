package io.extremum.rdf.triple.dao.jpa.model;

import graphql.annotations.annotationTypes.GraphQLField;
import io.extremum.common.annotation.ModelName;
import io.extremum.jpa.model.PostgresCommonModel;
import io.extremum.rdf.triple.model.ITriple;
import io.extremum.security.ExtremumRequiredRoles;
import io.extremum.security.NoDataSecurity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.Set;

@Entity
@Getter
@Setter
@ModelName("triple")
@ExtremumRequiredRoles(defaultAccess = "USER")
@Access(AccessType.FIELD)
@NoDataSecurity
public class Triple extends PostgresCommonModel implements ITriple {

    /**
     * RDF URI reference or a blank node.
     */
    private String subject;

    /**
     * RDF URI reference.
     */
    private String predicate;

    /**
     * RDF URI reference, a literal or a blank node.
     */
    @ElementCollection
    private Set<String> objects;

    public Triple(String subject, String predicate, Set<String> objects) {
        this.subject = subject;
        this.predicate = predicate;
        this.objects = objects;
    }

    public Triple() {

    }
}
