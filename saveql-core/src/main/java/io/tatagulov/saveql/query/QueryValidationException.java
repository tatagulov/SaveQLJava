package io.tatagulov.saveql.query;

public final class QueryValidationException extends RuntimeException {
    public QueryValidationException(String message) {
        super(message);
    }
}
