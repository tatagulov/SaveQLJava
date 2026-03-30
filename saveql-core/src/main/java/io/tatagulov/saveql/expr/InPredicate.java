package io.tatagulov.saveql.expr;

import java.util.List;
import java.util.Objects;

public record InPredicate(SimpleExpr<?> expr, boolean negated, List<SimpleExpr<?>> values) implements PredicateExpr {
    public InPredicate(SimpleExpr<?> expr, boolean negated, SimpleExpr<?>... values) {
        this(expr, negated, List.of(values));
    }

    public InPredicate {
        Objects.requireNonNull(expr, "expr");
        values = List.copyOf(Objects.requireNonNull(values, "values"));
        if (values.isEmpty()) {
            throw new IllegalArgumentException("IN requires at least one value");
        }
        for (SimpleExpr<?> value : values) {
            Objects.requireNonNull(value, "IN values must not contain null");
        }
    }
}
