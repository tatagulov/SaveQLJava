package io.tatagulov.saveql.schema;

@FunctionalInterface
public interface ColumnMapper<L, R> {
    <V> ColumnLike<R, V> map(ColumnLike<L, V> column);
}
