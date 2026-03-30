package io.tatagulov.saveql.schema;

import java.util.function.Consumer;

public record Cols1<L, T1>(ColumnLike<L, T1> c1) implements Cols<S1, L> {

    @Override
    public <R> void zip(Cols<S1, R> rightColumns, ColumnPairConsumer<L, R> consumer) {
        Cols1<R, T1> right = (Cols1<R, T1>) rightColumns;
        consumer.each(c1, right.c1);
    }

    @Override
    public void zip(Values<S1> values, ColumnValueConsumer<L> consumer) {
        Values1<T1> rowValues = (Values1<T1>) values;
        consumer.each(c1, rowValues.v1());
    }

    @Override
    public void forEach(Consumer<ColumnLike<L, ?>> forEachFunc) {
        forEachFunc.accept(this.c1);
    }

    @Override
    public <R> Cols<S1, R> map(ColumnMapper<L, R> mapper) {
        return new Cols1<>(mapper.map(c1));
    }
}
