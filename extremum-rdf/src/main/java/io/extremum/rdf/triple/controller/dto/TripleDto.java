package io.extremum.rdf.triple.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class TripleDto {
    @GraphQLField
    private String subject;

    @GraphQLField
    private String predicate;

    @JsonProperty("object")
    @GraphQLField
    @GraphQLName("object")
    private String obj;

    @GraphQLField
    private Boolean delete = false;
}
