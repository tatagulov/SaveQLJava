package io.tatagulov.saveql.api;

import io.tatagulov.saveql.expr.PredicateExpr;
import io.tatagulov.saveql.schema.TableDef;
import io.tatagulov.saveql.sql.CompiledSql;
import io.tatagulov.saveql.sql.DeleteSqlCompiler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public final class DeleteTerminalCommand<R, T extends TableDef<R, ?>> {
    private final T table;
    private final PredicateExpr where;

    private DeleteTerminalCommand(T table, PredicateExpr where) {
        this.table = Objects.requireNonNull(table, "table");
        this.where = Objects.requireNonNull(where, "where");
    }

    public static <R, T extends TableDef<R, ?>> DeleteTerminalCommand<R, T> where(T table, PredicateExpr where) {
        return new DeleteTerminalCommand<>(table, where);
    }

    public CompiledSql compile() {
        validate();
        return new DeleteSqlCompiler().compile(this);
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
    }

    public T table() {
        return table;
    }

    public PredicateExpr where() {
        return where;
    }
}
