package io.tatagulov.saveql.api.stage;

import io.tatagulov.saveql.api.mapping.MappedQueryBuilder;
import io.tatagulov.saveql.expr.Expr;

import java.util.function.Supplier;

public interface SelectStage<L> {
    BuildStage<L> select(Expr<?>... exprs);

    <T> MappedQueryBuilder<L, T> selectTo(Supplier<T> supplier);

    BuildStage<L> selectCount();
}
