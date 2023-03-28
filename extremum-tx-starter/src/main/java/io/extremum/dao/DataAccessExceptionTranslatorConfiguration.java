package io.extremum.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;

import java.util.List;

@Configuration
public class DataAccessExceptionTranslatorConfiguration {

    @Bean
    public DataAccessExceptionTranslators dataAccessExceptionTranslators(List<DataAccessExceptionTranslator<? extends DataAccessException>> translators) {
        return new DataAccessExceptionTranslators(translators);
    }
}
