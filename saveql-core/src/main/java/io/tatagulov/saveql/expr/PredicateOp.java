package io.tatagulov.saveql.expr;

public enum PredicateOp {
    AND("and"),
    OR("or");
    private final String sql;

    PredicateOp(String sql) {
        this.sql = sql;
    }

    public String sql() {
        return sql;
    }
}
