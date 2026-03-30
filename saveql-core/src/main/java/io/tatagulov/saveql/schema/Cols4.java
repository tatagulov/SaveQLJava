package io.tatagulov.saveql.schema;

import java.util.function.Consumer;

public record Cols4<L, T1, T2, T3, T4>(
        ColumnLike<L, T1> c1,
        ColumnLike<L, T2> c2,
        ColumnLike<L, T3> c3,
        ColumnLike<L, T4> c4
) implements Cols<S4, L> {

    @SuppressWarnings("unchecked")
    @Override
    public <R> void zip(Cols<S4, R> rightColumns, ColumnPairConsumer<L, R> consumer) {
        Cols4<R, T1, T2, T3, T4> right = (Cols4<R, T1, T2, T3, T4>) rightColumns;
        consumer.each(c1, right.c1);
        consumer.each(c2, right.c2);
        consumer.each(c3, right.c3);
        consumer.each(c4, right.c4);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void zip(Values<S4> values, ColumnValueConsumer<L> consumer) {
        Values4<T1, T2, T3, T4> rowValues = (Values4<T1, T2, T3, T4>) values;
        consumer.each(c1, rowValues.v1());
        consumer.each(c2, rowValues.v2());
        consumer.each(c3, rowValues.v3());
        consumer.each(c4, rowValues.v4());
    }

    @Override
    public void forEach(Consumer<ColumnLike<L, ?>> forEachFunc) {
        forEachFunc.accept(c1);
        forEachFunc.accept(c2);
        forEachFunc.accept(c3);
        forEachFunc.accept(c4);
    }

    @Override
    public <R> Cols<S4, R> map(ColumnMapper<L, R> mapper) {
        return new Cols4<>(
                mapper.map(c1),
                mapper.map(c2),
                mapper.map(c3),
                mapper.map(c4)
        );
    }
}
