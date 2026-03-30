package io.tatagulov.saveql.api;

import io.tatagulov.saveql.expr.PredicateExpr;
import io.tatagulov.saveql.query.QueryValidationException;
import io.tatagulov.saveql.schema.ColumnDef;
import io.tatagulov.saveql.schema.TableDef;
import io.tatagulov.saveql.sql.CompiledSql;
import io.tatagulov.saveql.sql.UpdateSqlCompiler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public final class UpdateTerminalCommand<R, T extends TableDef<R, ?>> {
    private final T table;
    private final LinkedHashMap<ColumnDef<T, ?>, Object> assignments;
    private final PredicateExpr where;

    private UpdateTerminalCommand(T table,
                                  Map<ColumnDef<T, ?>, Object> assignments,
                                  PredicateExpr where) {
        this.table = Objects.requireNonNull(table, "table");
        this.assignments = new LinkedHashMap<>(Objects.requireNonNull(assignments, "assignments"));
        this.where = where;
    }

    public static <R,T extends TableDef<R, ?>> UpdateTerminalCommand<R, T> where(
            T table,
            Map<ColumnDef<T, ?>, Object> assignments,
            PredicateExpr where
    ) {
        Objects.requireNonNull(where, "where");
        return new UpdateTerminalCommand<>(table, assignments, where);
    }

    public CompiledSql compile() {
        validate();
        return new UpdateSqlCompiler().compile(this);
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
        if (assignments.isEmpty()) {
            throw new QueryValidationException("Update for table " + table.name() + " does not contain any assignments");
        }
    }

    public T table() {
        return table;
    }

    public PredicateExpr where() {
        return where;
    }

    public List<ColumnDef<T, ?>> assignmentColumns() {
        return new ArrayList<>(assignments.keySet());
    }

    public Object assignmentValueOf(ColumnDef<?, ?> column) {
        return assignments.get(column);
    }
}
