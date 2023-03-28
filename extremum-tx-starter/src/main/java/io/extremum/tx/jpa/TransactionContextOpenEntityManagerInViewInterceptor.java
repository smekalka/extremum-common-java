package io.extremum.tx.jpa;

import io.extremum.common.tx.TransactionContextHolder;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.EntityManager;

@AllArgsConstructor
public class TransactionContextOpenEntityManagerInViewInterceptor extends OpenEntityManagerInViewInterceptor {

    TransactionHolder transactionHolder;
    TransactionContextJpaTransactionManager transactionManager;

    @Override
    protected EntityManager createEntityManager() throws IllegalStateException {
        if (TransactionContextHolder.getContext() != null) {
            EntityManager existingEntityManager = transactionHolder.getEntityManager(TransactionContextHolder.getContext().getTransactionId());
            if (existingEntityManager == null) {
                EntityManager entityManager = transactionManager.createEntityManagerForTransaction();
                transactionHolder.setEntityManger(TransactionContextHolder.getContext().getTransactionId(), entityManager);
                return entityManager;
            }
            return existingEntityManager;
        } else {
            return super.createEntityManager();
        }
    }

    @Override
    public void afterCompletion(WebRequest request, Exception ex) throws DataAccessException {
        if (TransactionContextHolder.getContext() == null) {
            super.afterCompletion(request, ex);
        } else {
            //Nothing todo here. Entity manager should be closed in  TransactionContextJpaTransactionManager
        }
    }
}