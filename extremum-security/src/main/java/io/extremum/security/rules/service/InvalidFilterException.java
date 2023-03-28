package io.extremum.security.rules.service;

import io.extremum.common.model.CollectionFilter;
import lombok.Getter;
import org.springframework.dao.DataAccessException;

public class InvalidFilterException extends DataAccessException {
    @Getter
    private final CollectionFilter filter;

    public InvalidFilterException(CollectionFilter filter) {
        super("Unable to apply filter " + filter.toString());
        this.filter = filter;
    }
}