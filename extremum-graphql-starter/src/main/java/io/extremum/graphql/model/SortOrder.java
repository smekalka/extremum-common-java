package io.extremum.graphql.model;

import graphql.annotations.annotationTypes.GraphQLField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SortOrder {
    @GraphQLField
    private Sort.Direction direction;
    @GraphQLField
    private String property;
}