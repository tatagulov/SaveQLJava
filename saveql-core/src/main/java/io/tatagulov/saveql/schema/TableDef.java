package io.tatagulov.saveql.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class TableDef<R, S extends Shape> implements TableLike<R, S> {
    private final String name;
    private final String alias;
    private final List<ColumnDef<R, ?>> columns = new ArrayList<>();

    protected TableDef(String name, String alias) {
        this.name = Objects.requireNonNull(name, "name");
        this.alias = alias;
    }

    protected final <T> ColumnDef<R, T> col(String columnName, Class<T> javaType) {
        ColumnDef<R, T> column = new ColumnDef<>(this, columnName, javaType);
        columns.add(column);
        return column;
    }

    protected final <T> ColumnDef<R, T> col(
            String columnName,
            Class<T> javaType,
            boolean nullable,
            boolean hasDefaultValue,
            boolean generated
    ) {
        ColumnDef<R, T> column = new ColumnDef<>(this, columnName, javaType, nullable, hasDefaultValue, generated);
        columns.add(column);
        return column;
    }

    public final String name() {
        return name;
    }

    public final String alias() {
        return alias;
    }

    public final boolean hasAlias() {
        return alias != null && !alias.isBlank();
    }

    @Override
    public final String qualifier() {
        return hasAlias() ? alias() : name();
    }

    public final List<ColumnDef<R, ?>> columns() {
        return List.copyOf(columns);
    }

    @Override
    public abstract Cols<S, R> primaryKeyCols();

    public abstract TableDef<R, S> as(String alias);
}
