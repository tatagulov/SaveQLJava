package io.tatagulov.saveql.api;

import io.tatagulov.saveql.query.QueryValidationException;
import io.tatagulov.saveql.schema.ColumnDef;
import io.tatagulov.saveql.schema.TableDef;
import io.tatagulov.saveql.sql.CompiledSql;
import io.tatagulov.saveql.sql.InsertSqlCompiler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class OneInsertCommand<T extends TableDef<?, ?>> {
    private final T table;
    private final LinkedHashMap<ColumnDef<T, ?>, Object> values = new LinkedHashMap<>();

    public OneInsertCommand(T table) {
        this.table = Objects.requireNonNull(table, "table");
    }

    public T table() {
        return table;
    }

    public <V> OneInsertCommand<T> set(ColumnDef<T, V> column, V value) {
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

    public CompiledSql compile() {
        validate();
        return new InsertSqlCompiler().compile(this);
    }

    public int execute(Connection connection) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        CompiledSql compiled = compile();

        try (PreparedStatement statement = connection.prepareStatement(compiled.sql())) {
            List<Object> params = compiled.params();
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            return statement.executeUpdate();
        }
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
