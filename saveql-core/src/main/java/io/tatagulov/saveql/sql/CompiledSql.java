package io.tatagulov.saveql.sql;

import java.util.List;
import java.util.Objects;

public class CompiledSql {
    private final String sql;
    private final List<Object> params;

    public CompiledSql(String sql, List<Object> params) {
        this.sql = Objects.requireNonNull(sql, "sql");
        this.params = List.copyOf(Objects.requireNonNull(params, "params"));
    }

    public String sql() {
        return sql;
    }

    public List<Object> params() {
        return params;
    }
}
