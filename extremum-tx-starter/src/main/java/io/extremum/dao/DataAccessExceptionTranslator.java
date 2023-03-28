package io.extremum.dao;

import io.extremum.common.exceptions.CommonException;
import org.springframework.dao.DataAccessException;

public interface DataAccessExceptionTranslator<E extends DataAccessException> {

    CommonException translate(DataAccessException exception);

    Class<E> getExceptionClass();
}
