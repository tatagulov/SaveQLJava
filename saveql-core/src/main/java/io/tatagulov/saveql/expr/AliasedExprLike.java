package io.tatagulov.saveql.expr;

public interface AliasedExprLike<T> extends Expr<T> {
    Expr<T> expr();

    String alias();

    @Override
    default Class<T> getType() {
        return expr().getType();
    }
}
