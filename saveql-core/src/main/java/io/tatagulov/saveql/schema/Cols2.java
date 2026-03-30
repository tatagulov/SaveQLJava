package io.tatagulov.saveql.schema;

import java.util.function.Consumer;

public record Cols2<L, T1, T2>(ColumnLike<L, T1> c1, ColumnLike<L, T2> c2) implements Cols<S2, L> {
    @SuppressWarnings("unchecked")
    @Override
    public <R> void zip(Cols<S2, R> rightColumns, ColumnPairConsumer<L, R> consumer) {
        Cols2<R, T1, T2> right = (Cols2<R, T1, T2>) rightColumns;
        consumer.each(c1, right.c1);
        consumer.each(c2, right.c2);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void zip(Values<S2> values, ColumnValueConsumer<L> consumer) {
        Values2<T1, T2> rowValues = (Values2<T1, T2>) values;
        consumer.each(c1, rowValues.v1());
        consumer.each(c2, rowValues.v2());
    }

    @Override
    public void forEach(Consumer<ColumnLike<L, ?>> forEachFunc) {
        forEachFunc.accept(this.c1);
        forEachFunc.accept(this.c2);
    }

    @Override
    public <R> Cols<S2, R> map(ColumnMapper<L, R> mapper) {
        return new Cols2<>(
                mapper.map(c1),
                mapper.map(c2)
        );
    }
}
