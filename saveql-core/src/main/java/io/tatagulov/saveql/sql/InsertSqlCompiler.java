package io.tatagulov.saveql.sql;

import io.tatagulov.saveql.api.InsertRow;
import io.tatagulov.saveql.api.OneInsertCommand;
import io.tatagulov.saveql.schema.ColumnDef;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class InsertSqlCompiler {
    public CompiledSql compile(OneInsertCommand<?> command) {
        command.validate();
        return compileColumns(command.table().name(), command.columnsInOrder(), command::valueOf);
    }

    public CompiledSql compile(InsertRow<?> row) {
        row.validate();
        return compileColumns(row.table().name(), row.columnsInOrder(), row::valueOf);
    }

    private CompiledSql compileColumns(
            String tableName,
            List<? extends ColumnDef<?, ?>> columns,
            java.util.function.Function<ColumnDef<?, ?>, Object> valueProvider
    ) {
        List<Object> params = new ArrayList<>(columns.size());
        String sql = "insert into " + tableName
                + " (" + columns.stream().map(ColumnDef::name).collect(Collectors.joining(", ")) + ")"
                + " values (" + columns.stream().map(column -> "?").collect(Collectors.joining(", ")) + ")";

        for (ColumnDef<?, ?> column : columns) {
            params.add(valueProvider.apply(column));
        }

        return new CompiledSql(sql, params);
    }
}
