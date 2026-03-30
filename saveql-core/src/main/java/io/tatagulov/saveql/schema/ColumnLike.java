package io.tatagulov.saveql.schema;

import io.tatagulov.saveql.expr.SimpleExpr;

public interface ColumnLike<R, T> extends SimpleExpr<T> {
    TableLike<R, ?> tableLike();

    String name();

    default String qualifiedName() {
        return tableLike().qualifier() + "." + name();
    }
}
