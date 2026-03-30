package io.tatagulov.saveql.expr;

import java.util.Objects;

public final class LiteralExpr<T> implements SimpleExpr<T> {
    private final T value;
    private final Class<T> type;

    public LiteralExpr(T value) {
        this.value = Objects.requireNonNull(value, "value");
        this.type = (Class<T>) value.getClass();
    }

    public LiteralExpr(T value, Class<T> type) {
        this.value = Objects.requireNonNull(value, "value");
        this.type = type;
    }

    public T value() {
        return value;
    }

    @Override
    public Class<T> getType() {
        return type;
    }
}
