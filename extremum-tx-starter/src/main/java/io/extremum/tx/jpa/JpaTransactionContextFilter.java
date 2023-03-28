package io.extremum.tx.jpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.exceptions.handler.DefaultExceptionHandler;
import io.extremum.common.tx.TransactionContext;
import io.extremum.common.tx.TransactionContextHolder;
import io.extremum.common.tx.TransactionRequest;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.tx.exceptions.TransactionAlreadyExistsException;
import io.extremum.tx.exceptions.TransactionIsNotAllowedException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.persistence.EntityManager;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class JpaTransactionContextFilter extends OncePerRequestFilter {

    public JpaTransactionContextFilter(int cookieTtl, ObjectMapper objectMapper, JpaTransactionManager transactionManager, TransactionHolder transactionHolder, DefaultExceptionHandler exceptionHandler) {
        this.cookieTtl = (int) Math.ceil(cookieTtl / 1000f);
        this.objectMapper = objectMapper;
        this.transactionManager = transactionManager;
        this.transactionHolder = transactionHolder;
        this.exceptionHandler = exceptionHandler;
    }

    private final int cookieTtl;

    private final ObjectMapper objectMapper;

    private final JpaTransactionManager transactionManager;
    private final TransactionHolder transactionHolder;
    private final DefaultExceptionHandler exceptionHandler;

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        initContextHolders(request, response);

        try {
            if (TransactionContextHolder.getContext() != null && TransactionContextHolder.getContext().getTransactionRequest().equals(TransactionRequest.ROLLBACK)) {
                EntityManager entityManager = transactionManager.getEntityManagerFactory().createEntityManager();
                if (entityManager != null) {
                    log.info("Rollback transaction {}", TransactionContextHolder.getContext().getTransactionId());
                    entityManager.getTransaction().rollback();
                    log.info("Close entity manager {}", TransactionContextHolder.getContext().getTransactionId());
                    entityManager.close();
                }
                PrintWriter writer = response.getWriter();
                TransactionContext context = TransactionContextHolder.getContext();
                if (context != null) {
                    writer.write(objectMapper.writeValueAsString(Response.ok(String.format("Transaction %s rolled back", TransactionContextHolder.getContext().getTransactionId()))));
                } else {
                    writer.write("Nothing");
                }

                return;
            }
            if (TransactionContextHolder.getContext() == null && request.getHeader("x-tx") != null && request.getHeader("x-tx").equals("rollback")) {
                PrintWriter writer = response.getWriter();
                writer.write(objectMapper.writeValueAsString(Response.ok("Nothing to rollback")));
                response.setStatus(400);

                return;
            }
            filterChain.doFilter(request, response);
        } finally {
            resetContextHolders();
            if (logger.isTraceEnabled()) {
                logger.info("Cleared thread-bound transaction context: " + request);
            }
        }
    }

    @SneakyThrows
    private void initContextHolders(HttpServletRequest request, HttpServletResponse response) {
        if (request.getHeaderNames() == null) {
            return;
        }
        String x_tx = request.getHeader("x-tx");
        if (x_tx != null && x_tx.equals("begin")) {

            Optional<Cookie> xtxCookie;
            if (request.getCookies() == null) {
                xtxCookie = Optional.empty();
            } else {
                xtxCookie = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("x-txid") && cookie.getValue() != null).findFirst();
            }

            String txId;
            if (xtxCookie.isPresent()) {
                txId = xtxCookie.get().getValue();
                EntityManager entityManager = transactionHolder.getEntityManager(txId);
                if (entityManager != null) {
                    ResponseEntity<Response> handle = exceptionHandler.handle(new TransactionAlreadyExistsException(txId));
                    PrintWriter writer = response.getWriter();
                    writer.write(objectMapper.writeValueAsString(handle.getBody()));
                    response.setStatus(handle.getStatusCodeValue());
                    return;
                }
            } else {
                txId = UUID.randomUUID().toString();
            }

            TransactionContextHolder.setContext(new TransactionContext(
                    TransactionRequest.BEGIN, txId
            ));
            Cookie cookie = new Cookie("x-txid", TransactionContextHolder.getContext().getTransactionId());
            cookie.setMaxAge(cookieTtl);
            cookie.setSecure(false);
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        Optional<Cookie> xtxCookie;
        if (request.getCookies() == null) {
            xtxCookie = Optional.empty();
        } else {
            xtxCookie = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("x-txid") && cookie.getValue() != null).findFirst();
        }

        if (x_tx != null && x_tx.equals("commit")) {
            if (!isGraphQLRequest(request)) {
                ResponseEntity<Response> handle = exceptionHandler.handle(new TransactionIsNotAllowedException("Commit is not allowed. Please use commit transaction endpoint", 400));
                PrintWriter writer = response.getWriter();
                writer.write(objectMapper.writeValueAsString(handle.getBody()));
                response.setStatus(handle.getStatusCodeValue());

                return;
            }

            xtxCookie.ifPresent(cookie ->
                    TransactionContextHolder.setContext(new TransactionContext(
                            TransactionRequest.COMMIT, cookie.getValue()
                    ))
            );

            Cookie cookie = new Cookie("x-txid", null);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        if (x_tx != null && x_tx.equals("rollback")) {
            xtxCookie.ifPresent(cookie ->
                    TransactionContextHolder.setContext(
                            new TransactionContext(TransactionRequest.ROLLBACK, cookie.getValue())
                    )
            );

            Cookie cookie = new Cookie("x-txid", null);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            response.setContentType("application/json");
        }
        if (x_tx == null) {
            xtxCookie.ifPresent(cookie -> {
                        TransactionContextHolder.setContext(
                                new TransactionContext(TransactionRequest.CONTINUATION, cookie.getValue())
                        );
                        Cookie updateCookie = new Cookie("x-txid", TransactionContextHolder.getContext().getTransactionId());
                        updateCookie.setMaxAge(cookieTtl);
                        updateCookie.setSecure(false);
                        updateCookie.setPath("/");
                        response.addCookie(updateCookie);
                    }
            );
        }
    }

    private boolean isGraphQLRequest(HttpServletRequest request) {
        return request.getServletPath().equals("/graphql");
    }

    private void resetContextHolders() {
        TransactionContextHolder.clear();
    }
}
