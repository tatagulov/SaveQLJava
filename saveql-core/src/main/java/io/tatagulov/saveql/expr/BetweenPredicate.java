package io.tatagulov.saveql.expr;

import java.util.Objects;

public record BetweenPredicate(SimpleExpr<?> expr, boolean negated, SimpleExpr<?> lower, SimpleExpr<?> upper)
        implements PredicateExpr {
    public BetweenPredicate {
        Objects.requireNonNull(expr, "expr");
        Objects.requireNonNull(lower, "lower");
        Objects.requireNonNull(upper, "upper");
    }
}
