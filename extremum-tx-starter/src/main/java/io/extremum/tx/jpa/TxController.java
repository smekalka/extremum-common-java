package io.extremum.tx.jpa;

import io.extremum.common.tx.TransactionContext;
import io.extremum.common.tx.TransactionContextHolder;
import io.extremum.common.tx.TransactionRequest;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.tx.exceptions.TransactionAlreadyExistsException;
import io.extremum.tx.exceptions.TransactionNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RestController
public class TxController {

    private final TransactionHolder transactionHolder;
    private final int cookieTtl;
    private final TransactionContextJpaTransactionManager txManager;

    public TxController(TransactionHolder transactionHolder, int cookieTtl, TransactionContextJpaTransactionManager txManager) {
        this.transactionHolder = transactionHolder;
        this.cookieTtl = (int) Math.ceil(cookieTtl / 1000f);
        this.txManager = txManager;
    }

    @GetMapping("/tx/begin")
    public Response begin(HttpServletResponse response) {
        String transactionId = UUID.randomUUID().toString();
        setCookie(transactionId, response);
        txManager.createEntityManagerForTransaction().getTransaction();

        return Response.ok();
    }

    @GetMapping("/tx/begin/{txId}")
    public Response beginWithId(@PathVariable String txId, HttpServletResponse response) {
        EntityManager entityManager = transactionHolder.getEntityManager(txId);
        if (entityManager != null) {
            throw new TransactionAlreadyExistsException(txId);
        }
        setCookie(txId, response);
        txManager.createEntityManagerForTransaction().getTransaction();

        return Response.ok();
    }

    private void setCookie(String txId, HttpServletResponse response) {
        TransactionContextHolder.setContext(new TransactionContext(TransactionRequest.BEGIN, txId));
        Cookie cookie = new Cookie("x-txid", txId);
        cookie.setMaxAge(cookieTtl);
        cookie.setSecure(false);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @PostMapping("/tx/commit/{txId}")
    public Response commit(@PathVariable String txId, HttpServletResponse response) {
        EntityManager entityManager = transactionHolder.getEntityManager(txId);
        if (entityManager == null) {
            throw new TransactionNotFoundException(txId);
        }
        EntityTransaction transaction = entityManager.getTransaction();

        if (transaction.isActive()) {
            transaction.commit();
        }
        entityManager.close();

        Cookie cookie = new Cookie("x-txid", null);
        cookie.setMaxAge(0);
        cookie.setSecure(false);
        cookie.setPath("/");
        response.addCookie(cookie);

        return Response.ok();
    }

    @PostMapping("/tx/rollback/{txId}")
    public Response rollback(@PathVariable String txId, HttpServletResponse response) {
        EntityManager entityManager = transactionHolder.getEntityManager(txId);
        if (entityManager == null) {
            throw new TransactionNotFoundException(txId);
        }
        EntityTransaction transaction = entityManager.getTransaction();
        if (transaction.isActive()) {
            transaction.rollback();
        }
        entityManager.close();

        Cookie cookie = new Cookie("x-txid", null);
        cookie.setMaxAge(0);
        cookie.setSecure(false);
        cookie.setPath("/");
        response.addCookie(cookie);

        return Response.ok();
    }
}
