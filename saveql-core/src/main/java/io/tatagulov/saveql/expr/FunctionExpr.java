package io.tatagulov.saveql.expr;

import java.util.Arrays;
import java.util.List;

public class FunctionExpr<T> implements SimpleExpr<T> {
    private final String name;
    private final List<SimpleExpr<?>> args;
    private final Class<T> type;

    public FunctionExpr(String name, Class<T> type, SimpleExpr<?>... args) {
        this.name = name;
        this.type = type;
        this.args = Arrays.asList(args);
    }

    public String name() {
        return name;
    }

    public List<SimpleExpr<?>> args() {
        return args;
    }

    @Override
    public Class<T> getType() {
        return type;
    }
}
