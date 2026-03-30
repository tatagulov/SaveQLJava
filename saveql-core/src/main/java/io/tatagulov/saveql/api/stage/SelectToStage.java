package io.tatagulov.saveql.api.stage;

import io.tatagulov.saveql.api.mapping.MappedQueryBuilder;
import io.tatagulov.saveql.expr.Expr;

import java.util.function.BiConsumer;

public interface SelectToStage<RootRow, T> {

    <M> MapStage<RootRow, T> map(Expr<M> expr, BiConsumer<T, M> setter);
}
