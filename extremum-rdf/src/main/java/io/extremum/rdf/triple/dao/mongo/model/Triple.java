package io.extremum.rdf.triple.dao.mongo.model;

import io.extremum.authentication.api.Roles;
import io.extremum.common.annotation.ModelName;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.rdf.triple.model.ITriple;
import io.extremum.security.ExtremumRequiredRoles;
import io.extremum.security.NoDataSecurity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;

/**
 * The RDF triple implementation.
 */
@Getter
@Document
@ModelName("triple")
@ExtremumRequiredRoles(defaultAccess = {Roles.ANONYMOUS})
@NoDataSecurity
@AllArgsConstructor
public class Triple extends MongoCommonModel implements ITriple {
    /**
     * RDF URI reference or a blank node.
     */
    private final String subject;

    /**
     * RDF URI reference.
     */
    private final String predicate;

    /**
     * RDF URI reference, a literal or a blank node.
     */
    private final Collection<String> objects;
}