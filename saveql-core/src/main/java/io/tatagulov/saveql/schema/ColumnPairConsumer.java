package io.tatagulov.saveql.schema;

@FunctionalInterface
public interface ColumnPairConsumer<L, R> {
    <T> void each(ColumnLike<L, T> leftColumn, ColumnLike<R, T> rightColumn);
}
