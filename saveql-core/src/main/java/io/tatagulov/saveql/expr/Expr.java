package io.tatagulov.saveql.expr;

public interface Expr<T> {
    Class<T> getType();
}
