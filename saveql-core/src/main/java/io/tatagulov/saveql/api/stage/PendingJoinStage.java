package io.tatagulov.saveql.api.stage;

import io.tatagulov.saveql.query.JoinCondition;
import io.tatagulov.saveql.query.JoinSpec;
import io.tatagulov.saveql.query.JoinType;
import io.tatagulov.saveql.query.Query;
import io.tatagulov.saveql.schema.Cols;
import io.tatagulov.saveql.schema.ColumnPairConsumer;
import io.tatagulov.saveql.schema.ColumnLike;
import io.tatagulov.saveql.schema.Shape;
import io.tatagulov.saveql.schema.TableLike;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class PendingJoinStage<Root, Left, Right, S extends Shape, PrevStage> implements JoinStage<Root, Left, Right, S, PrevStage> {
    protected final Query<Root> query;
    protected final PrevStage previousStage;
    protected final JoinType joinType;
    protected final TableLike<Right, S> table;
    protected final List<JoinSpec<?, ?, ?>> subJoins = new ArrayList<>();

    PendingJoinStage(Query<Root> query, PrevStage previousStage, JoinType joinType, TableLike<Right, S> table) {
        this.query = query;
        this.previousStage = previousStage;
        this.joinType = Objects.requireNonNull(joinType, "joinType");
        this.table = Objects.requireNonNull(table, "table");
    }

    @Override
    public <R, NextS extends Shape> JoinStage<Root, Right, R, NextS, JoinStage<Root, Left, Right, S, PrevStage>> join(TableLike<R, NextS> table) {
        return new PendingJoinStage<>(query, this, JoinType.INNER, table);
    }

    @Override
    public <R, NextS extends Shape> JoinStage<Root, Right, R, NextS, JoinStage<Root, Left, Right, S, PrevStage>> leftJoin(TableLike<R, NextS> table) {
        return new PendingJoinStage<>(query, this, JoinType.LEFT, table);
    }

    @Override
    public PrevStage by(Cols<S, Left> cols) {
        List<JoinCondition<Left, Right, ?>> conditions = new ArrayList<>();
        cols.zip(table.primaryKeyCols(), new ColumnPairConsumer<>() {
            @Override
            public <T> void each(ColumnLike<Left, T> leftColumn, ColumnLike<Right, T> rightColumn) {
                conditions.add(new JoinCondition<>(leftColumn, rightColumn));
            }
        });

        JoinSpec<Left, Right, S> resolved = new JoinSpec<>(joinType, table, conditions, List.of(), subJoins);
        attachToPrevious(resolved);
        return previousStage;
    }

    private void attachToPrevious(JoinSpec<?, ?, ?> join) {
        if (previousStage instanceof PendingJoinStage<?, ?, ?, ?, ?> pending) {
            pending.subJoins.add(join);
            return;
        }
        if (previousStage instanceof StagedQueryBuilder<?> builder) {
            builder.addJoin(join);
            return;
        }
        throw new IllegalStateException("Unknown previous stage: " + previousStage.getClass().getName());
    }
}
