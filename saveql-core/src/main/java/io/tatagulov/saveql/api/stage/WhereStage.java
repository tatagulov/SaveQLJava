package io.tatagulov.saveql.api.stage;

import io.tatagulov.saveql.expr.PredicateExpr;
import io.tatagulov.saveql.schema.Shape;
import io.tatagulov.saveql.schema.TableLike;
import io.tatagulov.saveql.schema.TableLike1;
import io.tatagulov.saveql.schema.TableLike2;
import io.tatagulov.saveql.schema.TableLike3;
import io.tatagulov.saveql.schema.TableLike4;
import io.tatagulov.saveql.schema.TableLike5;

public interface WhereStage<L> extends OrderStage<L> {
    <R, S extends Shape> JoinStage<L, L, R, S, WhereStage<L>> join(TableLike<R, S> table);

    <R, T1> JoinStage1<L, L, R, T1, WhereStage<L>> join(TableLike1<R, T1> table);

    <R, T1, T2> JoinStage2<L, L, R, T1, T2, WhereStage<L>> join(TableLike2<R, T1, T2> table);

    <R, T1, T2, T3> JoinStage3<L, L, R, T1, T2, T3, WhereStage<L>> join(TableLike3<R, T1, T2, T3> table);

    <R, T1, T2, T3, T4> JoinStage4<L, L, R, T1, T2, T3, T4, WhereStage<L>> join(TableLike4<R, T1, T2, T3, T4> table);

    <R, T1, T2, T3, T4, T5> JoinStage5<L, L, R, T1, T2, T3, T4, T5, WhereStage<L>> join(TableLike5<R, T1, T2, T3, T4, T5> table);

    <R, S extends Shape> JoinStage<L, L, R, S, WhereStage<L>> leftJoin(TableLike<R, S> table);

    <R, T1> JoinStage1<L, L, R, T1, WhereStage<L>> leftJoin(TableLike1<R, T1> table);

    <R, T1, T2> JoinStage2<L, L, R, T1, T2, WhereStage<L>> leftJoin(TableLike2<R, T1, T2> table);

    <R, T1, T2, T3> JoinStage3<L, L, R, T1, T2, T3, WhereStage<L>> leftJoin(TableLike3<R, T1, T2, T3> table);

    <R, T1, T2, T3, T4> JoinStage4<L, L, R, T1, T2, T3, T4, WhereStage<L>> leftJoin(TableLike4<R, T1, T2, T3, T4> table);

    <R, T1, T2, T3, T4, T5> JoinStage5<L, L, R, T1, T2, T3, T4, T5, WhereStage<L>> leftJoin(TableLike5<R, T1, T2, T3, T4, T5> table);

    OrderStage<L> where(PredicateExpr predicate);
}
