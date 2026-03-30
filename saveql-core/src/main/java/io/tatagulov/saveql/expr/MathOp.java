package io.tatagulov.saveql.expr;

public enum MathOp {
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    MOD("%");

    private final String sql;

    MathOp(String sql) {
        this.sql = sql;
    }

    public String sql() {
        return sql;
    }
}
