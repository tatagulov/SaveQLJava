package io.tatagulov.saveql.expr;

public final class AggregateFunctionExpr<T> extends FunctionExpr<T> implements AggregateExpr<T> {


    public AggregateFunctionExpr(String name, Class<T> type, SimpleExpr<?>... args) {
        super(name, type, args);
    }
}
