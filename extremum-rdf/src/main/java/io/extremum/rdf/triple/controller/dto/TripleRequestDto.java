package io.extremum.rdf.triple.controller.dto;

import io.extremum.sharedmodels.dto.RequestDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
public class TripleRequestDto implements RequestDto {

    private String subject;

    private String predicate;

    private Collection<String> objects;
}
