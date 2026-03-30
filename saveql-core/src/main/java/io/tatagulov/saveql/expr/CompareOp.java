package io.tatagulov.saveql.expr;

public enum CompareOp {
    EQ("="),
    NE("<>"),
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    LIKE("LIKE"),
    ILIKE("ILIKE");

    private final String sql;

    CompareOp(String sql) {
        this.sql = sql;
    }

    public String sql() {
        return sql;
    }
}
