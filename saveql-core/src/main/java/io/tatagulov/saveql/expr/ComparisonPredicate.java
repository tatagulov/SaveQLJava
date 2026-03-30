package io.tatagulov.saveql.expr;

import java.util.Objects;

public final class ComparisonPredicate implements PredicateExpr {
    private final SimpleExpr<?> left;
    private final CompareOp op;
    private final SimpleExpr<?> right;

    public ComparisonPredicate(SimpleExpr<?> left, CompareOp op, SimpleExpr<?> right) {
        this.left = Objects.requireNonNull(left, "left");
        this.op = Objects.requireNonNull(op, "op");
        this.right = Objects.requireNonNull(right, "right");
    }

    public SimpleExpr<?> left() {
        return left;
    }

    public CompareOp op() {
        return op;
    }

    public SimpleExpr<?> right() {
        return right;
    }
}
