package io.tatagulov.saveql.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CaseWhenExpr<T> implements SimpleExpr<T> {
    private final List<WhenThen<T>> branches;
    private final SimpleExpr<T> elseExpr;

    public CaseWhenExpr(PredicateExpr condition, SimpleExpr<T> value) {
        this(List.of(new WhenThen<>(condition, value)), null);
    }

    private CaseWhenExpr(List<WhenThen<T>> branches, SimpleExpr<T> elseExpr) {
        this.branches = List.copyOf(Objects.requireNonNull(branches, "branches"));
        if (this.branches.isEmpty()) {
            throw new IllegalArgumentException("CASE requires at least one WHEN branch");
        }
        this.elseExpr = elseExpr;
    }

    public List<WhenThen<T>> branches() {
        return branches;
    }

    public SimpleExpr<T> elseExpr() {
        return elseExpr;
    }

    public CaseWhenExpr<T> when(PredicateExpr condition, SimpleExpr<T> value) {
        List<WhenThen<T>> next = new ArrayList<>(branches);
        next.add(new WhenThen<>(condition, value));
        return new CaseWhenExpr<>(next, elseExpr);
    }

    public CaseWhenExpr<T> when(PredicateExpr condition, T value) {
        return when(condition, new LiteralExpr<>(value));
    }

    public CaseWhenExpr<T> otherwise(SimpleExpr<T> value) {
        return new CaseWhenExpr<>(branches, value);
    }

    public CaseWhenExpr<T> otherwise(T value) {
        return otherwise(new LiteralExpr<>(value));
    }

    @Override
    public Class<T> getType() {
        return branches.get(branches.size() - 1).value().getType();
    }

    public record WhenThen<T>(PredicateExpr condition, SimpleExpr<T> value) {
        public WhenThen {
            Objects.requireNonNull(condition, "condition");
            Objects.requireNonNull(value, "value");
        }
    }
}
