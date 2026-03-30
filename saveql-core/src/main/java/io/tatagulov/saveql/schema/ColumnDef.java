package io.tatagulov.saveql.schema;

import java.util.Objects;

public final class ColumnDef<R, T> implements ColumnLike<R, T> {
    private final TableLike<R, ?> tableLike;
    private final String name;
    private final Class<T> type;
    private final boolean nullable;
    private final boolean hasDefaultValue;
    private final boolean generated;

    ColumnDef(TableLike<R, ?> tableLike, String name, Class<T> type) {
        this(tableLike, name, type, true, false, false);
    }

    ColumnDef(TableLike<R, ?> tableLike, String name, Class<T> type, boolean nullable, boolean hasDefaultValue, boolean generated) {
        this.tableLike = Objects.requireNonNull(tableLike, "tableLike");
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "javaType");
        this.nullable = nullable;
        this.hasDefaultValue = hasDefaultValue;
        this.generated = generated;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TableLike<R, ?> tableLike() {
        return tableLike;
    }

    @Override
    public String qualifiedName() {
        return ColumnLike.super.qualifiedName();
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean hasDefaultValue() {
        return hasDefaultValue;
    }

    public boolean isGenerated() {
        return generated;
    }

    public boolean isRequiredForInsert() {
        return !nullable && !hasDefaultValue && !generated;
    }
}
