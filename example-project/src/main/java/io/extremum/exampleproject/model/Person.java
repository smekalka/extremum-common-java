package io.extremum.exampleproject.model;

import io.extremum.authentication.api.Roles;
import io.extremum.common.model.annotation.ModelName;
import io.extremum.mongo.model.MongoNamedModel;
import io.extremum.security.ExtremumRequiredRoles;
import io.extremum.security.NoDataSecurity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@ModelName("Person")
@ExtremumRequiredRoles(defaultAccess = Roles.ANONYMOUS)
@NoDataSecurity
@Getter
@Setter
public class Person extends MongoNamedModel {
    private String address;
}
