package io.tatagulov.saveql.api;

import io.tatagulov.saveql.query.QueryValidationException;
import io.tatagulov.saveql.schema.ColumnDef;
import io.tatagulov.saveql.schema.TableDef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class InsertRow<T extends TableDef<?, ?>> {
    private final T table;
    private final LinkedHashMap<ColumnDef<T, ?>, Object> values = new LinkedHashMap<>();

    private InsertRow(T table) {
        this.table = Objects.requireNonNull(table, "table");
    }

    public static <T extends TableDef<?, ?>> InsertRow<T> into(T table) {
        return new InsertRow<>(table);
    }

    public T table() {
        return table;
    }

    public <V> InsertRow<T> set(ColumnDef<T, V> column, V value) {
        Objects.requireNonNull(column, "column");
        if (column.tableLike() != table) {
            throw new QueryValidationException("Column " + column.qualifiedName() + " does not belong to insert table " + table.name());
        }
        if (column.isGenerated()) {
            throw new QueryValidationException("Column " + column.name() + " is generated and cannot be assigned explicitly");
        }
        values.put(column, value);
        return this;
    }

    public void validate() {
        if (values.isEmpty()) {
            throw new QueryValidationException("Insert for table " + table.name() + " does not contain any values");
        }

        for (ColumnDef<?, ?> column : table.columns()) {
            if (!column.isRequiredForInsert()) {
                continue;
            }

            if (!values.containsKey(column)) {
                throw new QueryValidationException("Required column " + column.name() + " is missing for table " + table.name());
            }

            if (values.get(column) == null) {
                throw new QueryValidationException("Required column " + column.name() + " cannot be null for table " + table.name());
            }
        }
    }

    public List<ColumnDef<T, ?>> columnsInOrder() {
        return new ArrayList<>(values.keySet());
    }

    public Object valueOf(ColumnDef<?, ?> column) {
        return values.get(column);
    }

    public Map<ColumnDef<T, ?>, Object> values() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
