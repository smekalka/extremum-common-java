package io.extremum.tx.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.exceptions.handler.DefaultExceptionHandler;
import io.extremum.tx.jpa.JpaTransactionContextFilter;
import io.extremum.tx.jpa.TransactionContextJpaTransactionManager;
import io.extremum.tx.jpa.TransactionContextOpenEntityManagerInViewInterceptor;
import io.extremum.tx.jpa.TransactionHolder;
import io.extremum.tx.jpa.TxController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.persistence.EntityManagerFactory;

@Configuration
@ConditionalOnProperty(prefix = "extremum", name = "transaction.enabled", havingValue = "true")
public class JpaTxConfiguration {

    @Bean("transactionManager")
    public PlatformTransactionManager transactionContextJpaTransactionManager(
            EntityManagerFactory entityManagerFactory,
            TransactionHolder transactionHolder
    ) {
        JpaTransactionManager txManager = new TransactionContextJpaTransactionManager(transactionHolder);
        txManager.setEntityManagerFactory(entityManagerFactory);

        return txManager;
    }

    @Bean
    public TransactionHolder transactionHolder(@Value("${extremum.transaction.ttl:500}") int txTtl) {
        return new TransactionHolder(txTtl);
    }

    @Bean
    public OpenEntityManagerInViewInterceptor myInterceptor(TransactionHolder transactionHolder, TransactionContextJpaTransactionManager jpaTransactionManager) {
        return new TransactionContextOpenEntityManagerInViewInterceptor(transactionHolder, jpaTransactionManager);
    }

    @Bean
    public WebMvcConfigurer openEntityManagerInViewInterceptorConfigurer(
            OpenEntityManagerInViewInterceptor interceptor) {
        return new WebMvcConfigurer() {

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addWebRequestInterceptor(interceptor);
            }

        };
    }

    @Bean
    public JpaTransactionContextFilter jpaTransactionContextFilter(
            @Value("${extremum.transaction.ttl:500}") int txTtl,
            ObjectMapper objectMapper, JpaTransactionManager transactionManager, TransactionHolder transactionHolder, DefaultExceptionHandler exceptionHandler
    ) {
        return new JpaTransactionContextFilter(txTtl, objectMapper, transactionManager, transactionHolder, exceptionHandler);
    }

    @Bean
    public TxController txController(TransactionHolder transactionHolder, @Value("${extremum.transaction.ttl:500}") int txTtl, TransactionContextJpaTransactionManager txManager){
        return new TxController(transactionHolder, txTtl, txManager);
    }
}
