package io.extremum.dao;

import io.extremum.common.exceptions.CommonException;
import org.springframework.dao.DataAccessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataAccessExceptionTranslators {

    private final Map<Class<? extends DataAccessException>, DataAccessExceptionTranslator<? extends DataAccessException>> translatorsMap = new HashMap<>();

    public DataAccessExceptionTranslators(List<DataAccessExceptionTranslator<? extends DataAccessException>> translators) {
        for (DataAccessExceptionTranslator<? extends DataAccessException> translator : translators) {
            translatorsMap.put(translator.getExceptionClass(), translator);
        }

    }

    public <T extends DataAccessException> CommonException translate(T exception) {
        DataAccessExceptionTranslator<? extends DataAccessException> translator = translatorsMap.get(exception.getClass());
        if (translator == null) {
            return new CommonException(exception.getMessage(), 500, exception);
        }

        return translatorsMap.get(exception.getClass()).translate(exception);
    }
}
