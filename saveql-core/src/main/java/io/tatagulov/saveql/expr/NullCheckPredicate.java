package io.tatagulov.saveql.expr;

import java.util.Objects;

public record NullCheckPredicate(SimpleExpr<?> expr, NullCheckOp op) implements PredicateExpr {
    public NullCheckPredicate {
        Objects.requireNonNull(expr, "expr");
        Objects.requireNonNull(op, "op");
    }
}
