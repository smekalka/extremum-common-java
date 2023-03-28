package io.extremum.tx.exceptions;

public class TransactionIsNotAllowedException extends TransactionException{
    public TransactionIsNotAllowedException(String message, int status) {
        super(message, status);
    }
}