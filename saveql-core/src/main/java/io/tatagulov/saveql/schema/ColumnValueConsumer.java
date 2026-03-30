package io.tatagulov.saveql.schema;

@FunctionalInterface
public interface ColumnValueConsumer<L> {
    <T> void each(ColumnLike<L, T> leftColumn, T value);
}
