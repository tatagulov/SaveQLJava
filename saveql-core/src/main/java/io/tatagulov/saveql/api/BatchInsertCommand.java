package io.tatagulov.saveql.api;

import io.tatagulov.saveql.query.QueryValidationException;
import io.tatagulov.saveql.schema.ColumnDef;
import io.tatagulov.saveql.schema.TableDef;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class BatchInsertCommand<T extends TableDef<?, ?>> {
    private final T table;
    private final List<InsertRow<T>> rows;
    private int batchSize = 1000;

    public BatchInsertCommand(T table, List<InsertRow<T>> rows) {
        this.table = Objects.requireNonNull(table, "table");
        this.rows = List.copyOf(Objects.requireNonNull(rows, "rows"));
    }

    public BatchInsertCommand<T> batchSize(int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than zero");
        }
        this.batchSize = batchSize;
        return this;
    }

    public int[] execute(Connection connection) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        validate();

        List<ColumnDef<T, ?>> columns = normalizedColumns();
        String sql = "insert into " + table.name()
                + " (" + columns.stream().map(ColumnDef::name).collect(Collectors.joining(", ")) + ")"
                + " values (" + columns.stream().map(column -> "?").collect(Collectors.joining(", ")) + ")";
        List<Integer> affected = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int pending = 0;
            for (InsertRow<T> row : rows) {
                for (int i = 0; i < columns.size(); i++) {
                    statement.setObject(i + 1, row.valueOf(columns.get(i)));
                }
                statement.addBatch();
                pending++;

                if (pending == batchSize) {
                    flushBatch(statement, affected);
                    pending = 0;
                }
            }

            if (pending > 0) {
                flushBatch(statement, affected);
            }
        }

        return affected.stream().mapToInt(Integer::intValue).toArray();
    }

    public void validate() {
        if (rows.isEmpty()) {
            throw new QueryValidationException("Batch insert for table " + table.name() + " does not contain any rows");
        }

        for (InsertRow<T> row : rows) {
            if (row.table() != table) {
                throw new QueryValidationException("Batch row belongs to table " + row.table().name() + " instead of " + table.name());
            }
            row.validate();
        }
    }

    private List<ColumnDef<T, ?>> normalizedColumns() {
        List<ColumnDef<T, ?>> columns = new ArrayList<>();
        for (ColumnDef<?, ?> rawColumn : table.columns()) {
            @SuppressWarnings("unchecked")
            ColumnDef<T, ?> tableColumn = (ColumnDef<T, ?>) rawColumn;
            boolean presentInAnyRow = rows.stream().anyMatch(row -> row.values().containsKey(tableColumn));
            if (presentInAnyRow) {
                columns.add(tableColumn);
            }
        }
        return columns;
    }

    private void flushBatch(PreparedStatement statement, List<Integer> affected) throws SQLException {
        int[] chunk = statement.executeBatch();
        for (int value : chunk) {
            affected.add(value);
        }
    }
}
