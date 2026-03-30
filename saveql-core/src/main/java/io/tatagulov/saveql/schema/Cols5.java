package io.tatagulov.saveql.schema;

import java.util.function.Consumer;

public record Cols5<L, T1, T2, T3, T4, T5>(
        ColumnLike<L, T1> c1,
        ColumnLike<L, T2> c2,
        ColumnLike<L, T3> c3,
        ColumnLike<L, T4> c4,
        ColumnLike<L, T5> c5
) implements Cols<S5, L> {

    @SuppressWarnings("unchecked")
    @Override
    public <R> void zip(Cols<S5, R> rightColumns, ColumnPairConsumer<L, R> consumer) {
        Cols5<R, T1, T2, T3, T4, T5> right = (Cols5<R, T1, T2, T3, T4, T5>) rightColumns;
        consumer.each(c1, right.c1);
        consumer.each(c2, right.c2);
        consumer.each(c3, right.c3);
        consumer.each(c4, right.c4);
        consumer.each(c5, right.c5);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void zip(Values<S5> values, ColumnValueConsumer<L> consumer) {
        Values5<T1, T2, T3, T4, T5> rowValues = (Values5<T1, T2, T3, T4, T5>) values;
        consumer.each(c1, rowValues.v1());
        consumer.each(c2, rowValues.v2());
        consumer.each(c3, rowValues.v3());
        consumer.each(c4, rowValues.v4());
        consumer.each(c5, rowValues.v5());
    }

    @Override
    public void forEach(Consumer<ColumnLike<L, ?>> forEachFunc) {
        forEachFunc.accept(c1);
        forEachFunc.accept(c2);
        forEachFunc.accept(c3);
        forEachFunc.accept(c4);
        forEachFunc.accept(c5);
    }

    @Override
    public <R> Cols<S5, R> map(ColumnMapper<L, R> mapper) {
        return new Cols5<>(
                mapper.map(c1),
                mapper.map(c2),
                mapper.map(c3),
                mapper.map(c4),
                mapper.map(c5)
        );
    }
}
