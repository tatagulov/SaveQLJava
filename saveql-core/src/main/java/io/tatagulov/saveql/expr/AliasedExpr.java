package io.tatagulov.saveql.expr;

import java.util.Objects;

public final class AliasedExpr<T> implements AliasedExprLike<T> {
    private final Expr<T> expr;
    private final String alias;

    public AliasedExpr(Expr<T> expr, String alias) {
        this.expr = Objects.requireNonNull(expr, "expr");
        this.alias = Objects.requireNonNull(alias, "alias");
    }

    public Expr<T> expr() {
        return expr;
    }

    public String alias() {
        return alias;
    }
}
