package io.tatagulov.saveql.api;

import io.tatagulov.saveql.api.stage.WhereStage;
import io.tatagulov.saveql.api.stage.StagedQueryBuilder;
import io.tatagulov.saveql.expr.*;
import io.tatagulov.saveql.schema.*;

public final class DSL {
    private DSL() {
    }

    public static <RootRow> WhereStage<RootRow> from(TableLike<RootRow, ?> root) {
        return StagedQueryBuilder.from(root);
    }

    public static <T extends TableDef<?, ?>> InsertCommand<T> insertInto(T table) {
        return new InsertCommand<>(table);
    }

    public static <T extends TableDef<?, ?>> InsertRow<T> insertRow(T table) {
        return InsertRow.into(table);
    }

    public static <R, T1, T extends TableDef1<R, T1>> UpdateCommand1<R, T1, T> update(T table) {
        return new UpdateCommand1<>(table);
    }

    public static <R, T1, T2, T extends TableDef2<R, T1, T2>> UpdateCommand2<R, T1, T2, T> update(T table) {
        return new UpdateCommand2<>(table);
    }

    public static <R, T1, T2, T3, T extends TableDef3<R, T1, T2, T3>> UpdateCommand3<R, T1, T2, T3, T> update(T table) {
        return new UpdateCommand3<>(table);
    }

    public static <R, T1, T2, T3, T4, T extends TableDef4<R, T1, T2, T3, T4>> UpdateCommand4<R, T1, T2, T3, T4, T> update(T table) {
        return new UpdateCommand4<>(table);
    }

    public static <R, T1, T2, T3, T4, T5, T extends TableDef5<R, T1, T2, T3, T4, T5>> UpdateCommand5<R, T1, T2, T3, T4, T5, T> update(T table) {
        return new UpdateCommand5<>(table);
    }

    public static <R, S extends Shape, T extends TableDef<R, S>> UpdateCommand<R, S, T> update(T table) {
        return new UpdateCommand<>(table);
    }

    public static <R, T1, T extends TableDef1<R, T1>> DeleteCommand1<R, T1, T> delete(T table) {
        return new DeleteCommand1<>(table);
    }

    public static <R, T1, T2, T extends TableDef2<R, T1, T2>> DeleteCommand2<R, T1, T2, T> delete(T table) {
        return new DeleteCommand2<>(table);
    }

    public static <R, T1, T2, T3, T extends TableDef3<R, T1, T2, T3>> DeleteCommand3<R, T1, T2, T3, T> delete(T table) {
        return new DeleteCommand3<>(table);
    }

    public static <R, T1, T2, T3, T4, T extends TableDef4<R, T1, T2, T3, T4>> DeleteCommand4<R, T1, T2, T3, T4, T> delete(T table) {
        return new DeleteCommand4<>(table);
    }

    public static <R, T1, T2, T3, T4, T5, T extends TableDef5<R, T1, T2, T3, T4, T5>> DeleteCommand5<R, T1, T2, T3, T4, T5, T> delete(T table) {
        return new DeleteCommand5<>(table);
    }

    public static <R, S extends Shape, T extends TableDef<R, S>> DeleteCommand<R, S, T> delete(T table) {
        return new DeleteCommand<>(table);
    }

    public static <T> LiteralExpr<T> val(T value) {
        return new LiteralExpr<>(value);
    }

    public static <T> CaseWhenExpr<T> caseWhen(PredicateExpr condition, SimpleExpr<T> value) {
        return new CaseWhenExpr<>(condition, value);
    }

    public static <T> CaseWhenExpr<T> caseWhen(PredicateExpr condition, T value) {
        return caseWhen(condition, val(value));
    }

    public static <N extends Number> AggregateFunctionExpr<N> sum(SimpleExpr<N> arg) {
        return new AggregateFunctionExpr<>("sum", arg.getType(), arg);
    }

    public static <N extends Number> AggregateFunctionExpr<N> avg(SimpleExpr<N> arg) {
        return new AggregateFunctionExpr<>("avg", arg.getType(), arg);
    }

    public static <T> AggregateFunctionExpr<T> min(SimpleExpr<T> arg) {
        return new AggregateFunctionExpr<>("min", arg.getType(), arg);
    }

    public static <T> AggregateFunctionExpr<T> max(SimpleExpr<T> arg) {
        return new AggregateFunctionExpr<>("max", arg.getType(), arg);
    }

    public static AggregateFunctionExpr<Long> count(SimpleExpr<?> arg) {
        return new AggregateFunctionExpr<>("count", Long.class, arg);
    }

    public static AggregateFunctionExpr<Boolean> boolAnd(SimpleExpr<Boolean> arg) {
        return new AggregateFunctionExpr<>("bool_and", arg.getType(), arg);
    }

    public static AggregateFunctionExpr<Boolean> boolOr(SimpleExpr<Boolean> arg) {
        return new AggregateFunctionExpr<>("bool_or", arg.getType(), arg);
    }

    public static <N extends Number> BinaryMathExpr<N> add(SimpleExpr<N> left, SimpleExpr<N> right) {
        return new BinaryMathExpr<>(left, MathOp.ADD, right);
    }

    public static <N extends Number> BinaryMathExpr<N> add(SimpleExpr<N> left, N right) {
        return add(left, val(right));
    }

    public static <N extends Number> BinaryMathExpr<N> sub(SimpleExpr<N> left, SimpleExpr<N> right) {
        return new BinaryMathExpr<>(left, MathOp.SUB, right);
    }

    public static <N extends Number> BinaryMathExpr<N> sub(SimpleExpr<N> left, N right) {
        return sub(left, val(right));
    }

    public static <N extends Number> BinaryMathExpr<N> mul(SimpleExpr<N> left, SimpleExpr<N> right) {
        return new BinaryMathExpr<>(left, MathOp.MUL, right);
    }

    public static <N extends Number> BinaryMathExpr<N> mul(SimpleExpr<N> left, N right) {
        return mul(left, val(right));
    }

    public static <N extends Number> BinaryMathExpr<N> div(SimpleExpr<N> left, SimpleExpr<N> right) {
        return new BinaryMathExpr<>(left, MathOp.DIV, right);
    }

    public static <N extends Number> BinaryMathExpr<N> div(SimpleExpr<N> left, N right) {
        return div(left, val(right));
    }

    public static <N extends Number> BinaryMathExpr<N> mod(SimpleExpr<N> left, SimpleExpr<N> right) {
        return new BinaryMathExpr<>(left, MathOp.MOD, right);
    }

    public static <N extends Number> BinaryMathExpr<N> mod(SimpleExpr<N> left, N right) {
        return mod(left, val(right));
    }
}
