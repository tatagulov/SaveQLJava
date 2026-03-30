package io.tatagulov.saveql.schema;

import java.util.function.Consumer;

public record Cols3<L, T1, T2, T3>(
        ColumnLike<L, T1> c1,
        ColumnLike<L, T2> c2,
        ColumnLike<L, T3> c3
) implements Cols<S3, L> {

    @SuppressWarnings("unchecked")
    @Override
    public <R> void zip(Cols<S3, R> rightColumns, ColumnPairConsumer<L, R> consumer) {
        Cols3<R, T1, T2, T3> right = (Cols3<R, T1, T2, T3>) rightColumns;
        consumer.each(c1, right.c1);
        consumer.each(c2, right.c2);
        consumer.each(c3, right.c3);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void zip(Values<S3> values, ColumnValueConsumer<L> consumer) {
        Values3<T1, T2, T3> rowValues = (Values3<T1, T2, T3>) values;
        consumer.each(c1, rowValues.v1());
        consumer.each(c2, rowValues.v2());
        consumer.each(c3, rowValues.v3());
    }

    @Override
    public void forEach(Consumer<ColumnLike<L, ?>> forEachFunc) {
        forEachFunc.accept(c1);
        forEachFunc.accept(c2);
        forEachFunc.accept(c3);
    }

    @Override
    public <R> Cols<S3, R> map(ColumnMapper<L, R> mapper) {
        return new Cols3<>(
                mapper.map(c1),
                mapper.map(c2),
                mapper.map(c3)
        );
    }
}
