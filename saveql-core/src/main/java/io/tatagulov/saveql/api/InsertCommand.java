package io.tatagulov.saveql.api;

import io.tatagulov.saveql.schema.ColumnDef;
import io.tatagulov.saveql.schema.TableDef;

import java.util.List;
import java.util.Objects;

public final class InsertCommand<T extends TableDef<?, ?>> {
    private final T table;

    public InsertCommand(T table) {
        this.table = Objects.requireNonNull(table, "table");
    }

    public <V> OneInsertCommand<T> set(ColumnDef<T, V> column, V value) {
        return new OneInsertCommand<>(table).set(column, value);
    }

    public BatchInsertCommand<T> batch(List<InsertRow<T>> rows) {
        return new BatchInsertCommand<>(table, rows);
    }
}
