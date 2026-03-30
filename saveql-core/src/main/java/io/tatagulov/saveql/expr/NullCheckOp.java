package io.tatagulov.saveql.expr;

public enum NullCheckOp {
    IS_NULL("IS NULL"),
    IS_NOT_NULL("IS NOT NULL");

    private final String sql;

    NullCheckOp(String sql) {
        this.sql = sql;
    }

    public String sql() {
        return sql;
    }
}
