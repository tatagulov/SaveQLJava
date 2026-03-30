package io.tatagulov.saveql.query;

import io.tatagulov.saveql.expr.SimpleExpr;

import java.util.Objects;

public final class OrderItem {
    private final SimpleExpr<?> expr;
    private final OrderDirection direction;
    private final NullOrder nullOrder;

    public OrderItem(SimpleExpr<?> expr, OrderDirection direction) {
        this(expr, direction, NullOrder.DEFAULT);
    }

    public OrderItem(SimpleExpr<?> expr, OrderDirection direction, NullOrder nullOrder) {
        this.expr = Objects.requireNonNull(expr, "expr");
        this.direction = Objects.requireNonNull(direction, "direction");
        this.nullOrder = Objects.requireNonNull(nullOrder, "nullOrder");
    }

    public SimpleExpr<?> expr() {
        return expr;
    }

    public OrderDirection direction() {
        return direction;
    }

    public NullOrder nullOrder() {
        return nullOrder;
    }

    public OrderItem nullsFirst() {
        return new OrderItem(expr, direction, NullOrder.NULLS_FIRST);
    }

    public OrderItem nullsLast() {
        return new OrderItem(expr, direction, NullOrder.NULLS_LAST);
    }
}
