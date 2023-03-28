package io.extremum.tx.exceptions;

import io.extremum.common.exceptions.CommonException;

public class TransactionException extends CommonException {
    public TransactionException(String message, int status) {
        super(message, status);
    }
}
