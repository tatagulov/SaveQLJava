package io.tatagulov.saveql.expr;

import io.tatagulov.saveql.query.OrderDirection;
import io.tatagulov.saveql.query.OrderItem;
import java.util.ArrayList;
import java.util.List;

public interface SimpleExpr<T> extends Expr<T> {
    default ComparisonPredicate eq(SimpleExpr<T> right) {
        return new ComparisonPredicate(this, CompareOp.EQ, right);
    }

    default ComparisonPredicate eq(T right) {
        return eq(new LiteralExpr<>(right));
    }

    default ComparisonPredicate gt(SimpleExpr<T> right) {
        return new ComparisonPredicate(this, CompareOp.GT, right);
    }

    default ComparisonPredicate gt(T right) {
        return gt(new LiteralExpr<>(right));
    }

    default ComparisonPredicate ne(SimpleExpr<T> right) {
        return new ComparisonPredicate(this, CompareOp.NE, right);
    }

    default ComparisonPredicate ne(T right) {
        return ne(new LiteralExpr<>(right));
    }

    default ComparisonPredicate gte(SimpleExpr<T> right) {
        return new ComparisonPredicate(this, CompareOp.GTE, right);
    }

    default ComparisonPredicate gte(T right) {
        return gte(new LiteralExpr<>(right));
    }

    default ComparisonPredicate lt(SimpleExpr<T> right) {
        return new ComparisonPredicate(this, CompareOp.LT, right);
    }

    default ComparisonPredicate lt(T right) {
        return lt(new LiteralExpr<>(right));
    }

    default ComparisonPredicate lte(SimpleExpr<T> right) {
        return new ComparisonPredicate(this, CompareOp.LTE, right);
    }

    default ComparisonPredicate lte(T right) {
        return lte(new LiteralExpr<>(right));
    }

    default ComparisonPredicate like(String pattern) {
        return new ComparisonPredicate(this, CompareOp.LIKE, new LiteralExpr<>(pattern));
    }

    default ComparisonPredicate ilike(String pattern) {
        return new ComparisonPredicate(this, CompareOp.ILIKE, new LiteralExpr<>(pattern));
    }

    default NullCheckPredicate isNull() {
        return new NullCheckPredicate(this, NullCheckOp.IS_NULL);
    }

    default NullCheckPredicate isNotNull() {
        return new NullCheckPredicate(this, NullCheckOp.IS_NOT_NULL);
    }

    default InPredicate in(List<? extends T> values) {
        return new InPredicate(this, false, wrapLiterals(values));
    }

    default InPredicate notIn(List<? extends T> values) {
        return new InPredicate(this, true, wrapLiterals(values));
    }

    default InPredicate in(SimpleExpr<?>... values) {
        return new InPredicate(this, false, values);
    }

    default InPredicate notIn(SimpleExpr<?>... values) {
        return new InPredicate(this, true, values);
    }

    default BetweenPredicate between(T lower, T upper) {
        return between(new LiteralExpr<>(lower), new LiteralExpr<>(upper));
    }

    default BetweenPredicate notBetween(T lower, T upper) {
        return notBetween(new LiteralExpr<>(lower), new LiteralExpr<>(upper));
    }

    default BetweenPredicate between(SimpleExpr<?> lower, SimpleExpr<?> upper) {
        return new BetweenPredicate(this, false, lower, upper);
    }

    default BetweenPredicate notBetween(SimpleExpr<?> lower, SimpleExpr<?> upper) {
        return new BetweenPredicate(this, true, lower, upper);
    }

    private List<SimpleExpr<?>> wrapLiterals(List<? extends T> values) {
        List<SimpleExpr<?>> wrapped = new ArrayList<>(values.size());
        for (T value : values) {
            wrapped.add(new LiteralExpr<>(value));
        }
        return wrapped;
    }

    default AliasedExpr<T> as(String alias) {
        return new AliasedExpr<>(this, alias);
    }

    default OrderItem asc() {
        return new OrderItem(this, OrderDirection.ASC);
    }

    default OrderItem desc() {
        return new OrderItem(this, OrderDirection.DESC);
    }
}
