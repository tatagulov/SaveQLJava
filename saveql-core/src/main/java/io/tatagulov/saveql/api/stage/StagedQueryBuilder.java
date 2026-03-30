package io.tatagulov.saveql.api.stage;

import io.tatagulov.saveql.api.mapping.MappedQueryBuilder;
import io.tatagulov.saveql.expr.Expr;
import io.tatagulov.saveql.expr.PredicateExpr;
import io.tatagulov.saveql.expr.SimpleExpr;
import io.tatagulov.saveql.query.JoinSpec;
import io.tatagulov.saveql.query.JoinType;
import io.tatagulov.saveql.query.OrderItem;
import io.tatagulov.saveql.query.Query;
import io.tatagulov.saveql.schema.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static io.tatagulov.saveql.api.DSL.count;

public final class StagedQueryBuilder<RootRow> implements WhereStage<RootRow>, BuildStage<RootRow> {
    private final Query<RootRow> query;

    private StagedQueryBuilder(TableLike<RootRow, ?> root) {
        this.query = new Query<>(root);
    }

    public static <RootRow> WhereStage<RootRow> from(TableLike<RootRow, ?> root) {
        return new StagedQueryBuilder<>(root);
    }

    @Override
    public <JoinedRow, S extends Shape> JoinStage<RootRow, RootRow, JoinedRow, S, WhereStage<RootRow>> join(TableLike<JoinedRow, S> table) {
        return new PendingJoinStage<>(query, this, JoinType.INNER, table);
    }

    @Override
    public <JoinedRow, T1> JoinStage1<RootRow, RootRow, JoinedRow, T1, WhereStage<RootRow>> join(TableLike1<JoinedRow, T1> table) {
        return new PendingJoinStage1<>(query, this, JoinType.INNER, table);
    }

    @Override
    public <JoinedRow, T1, T2> JoinStage2<RootRow, RootRow, JoinedRow, T1, T2, WhereStage<RootRow>> join(TableLike2<JoinedRow, T1, T2> table) {
        return new PendingJoinStage2<>(query, this, JoinType.INNER, table);
    }

    @Override
    public <JoinedRow, T1, T2, T3> JoinStage3<RootRow, RootRow, JoinedRow, T1, T2, T3, WhereStage<RootRow>> join(TableLike3<JoinedRow, T1, T2, T3> table) {
        return new PendingJoinStage3<>(query, this, JoinType.INNER, table);
    }

    @Override
    public <JoinedRow, T1, T2, T3, T4> JoinStage4<RootRow, RootRow, JoinedRow, T1, T2, T3, T4, WhereStage<RootRow>> join(TableLike4<JoinedRow, T1, T2, T3, T4> table) {
        return new PendingJoinStage4<>(query, this, JoinType.INNER, table);
    }

    @Override
    public <JoinedRow, T1, T2, T3, T4, T5> JoinStage5<RootRow, RootRow, JoinedRow, T1, T2, T3, T4, T5, WhereStage<RootRow>> join(TableLike5<JoinedRow, T1, T2, T3, T4, T5> table) {
        return new PendingJoinStage5<>(query, this, JoinType.INNER, table);
    }

    @Override
    public <JoinedRow, S extends Shape> JoinStage<RootRow, RootRow, JoinedRow, S, WhereStage<RootRow>> leftJoin(TableLike<JoinedRow, S> table) {
        return new PendingJoinStage<>(query, this, JoinType.LEFT, table);
    }

    @Override
    public <JoinedRow, T1> JoinStage1<RootRow, RootRow, JoinedRow, T1, WhereStage<RootRow>> leftJoin(TableLike1<JoinedRow, T1> table) {
        return new PendingJoinStage1<>(query, this, JoinType.LEFT, table);
    }

    @Override
    public <JoinedRow, T1, T2> JoinStage2<RootRow, RootRow, JoinedRow, T1, T2, WhereStage<RootRow>> leftJoin(TableLike2<JoinedRow, T1, T2> table) {
        return new PendingJoinStage2<>(query, this, JoinType.LEFT, table);
    }

    @Override
    public <JoinedRow, T1, T2, T3> JoinStage3<RootRow, RootRow, JoinedRow, T1, T2, T3, WhereStage<RootRow>> leftJoin(TableLike3<JoinedRow, T1, T2, T3> table) {
        return new PendingJoinStage3<>(query, this, JoinType.LEFT, table);
    }

    @Override
    public <JoinedRow, T1, T2, T3, T4> JoinStage4<RootRow, RootRow, JoinedRow, T1, T2, T3, T4, WhereStage<RootRow>> leftJoin(TableLike4<JoinedRow, T1, T2, T3, T4> table) {
        return new PendingJoinStage4<>(query, this, JoinType.LEFT, table);
    }

    @Override
    public <JoinedRow, T1, T2, T3, T4, T5> JoinStage5<RootRow, RootRow, JoinedRow, T1, T2, T3, T4, T5, WhereStage<RootRow>> leftJoin(TableLike5<JoinedRow, T1, T2, T3, T4, T5> table) {
        return new PendingJoinStage5<>(query, this, JoinType.LEFT, table);
    }


    @Override
    public BuildStage<RootRow> select(Expr<?>... exprs) {
        query.select(exprs);
        return this;
    }

    @Override
    public <T> MappedQueryBuilder<RootRow, T> selectTo(Supplier<T> supplier) {
        return new MappedQueryBuilder<>(query, supplier);
    }

    @Override
    public OrderStage<RootRow> where(PredicateExpr predicate) {
        query.where(predicate);
        return this;
    }

    @Override
    public LimitStage<RootRow> orderBy(OrderItem... items) {
        query.orderBy(items);
        return this;
    }

    @Override
    public OffsetStage<RootRow> limit(int limit) {
        query.limit(limit);
        return this;
    }

    @Override
    public SelectStage<RootRow> offset(int offset) {
        query.offset(offset);
        return this;
    }

    @Override
    public BuildStage<RootRow> selectCount() {
        query.select(count(firstPrimaryKeyExpr(query.root())));
        return this;
    }

    @Override
    public Query<RootRow> build() {
        return query;
    }

    void addJoin(JoinSpec<?, ?, ?> join) {
        query.addJoin(join);
    }

    private static SimpleExpr<?> firstPrimaryKeyExpr(TableLike<?, ?> table) {
        List<ColumnLike<?, ?>> columns = new ArrayList<>();
        table.primaryKeyCols().forEach(columns::add);
        if (columns.isEmpty()) {
            throw new IllegalStateException("Table primary key is empty: " + table.qualifier());
        }
        return columns.get(0);
    }

}
