package io.extremum.tx.config;

import io.extremum.tx.exceptions.TransactionExceptionHandlers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(JpaTxConfiguration.class)
public class TxConfiguration {

    @Bean
    public TransactionExceptionHandlers transactionExceptionHandlers() {
        return new TransactionExceptionHandlers();
    }
}
