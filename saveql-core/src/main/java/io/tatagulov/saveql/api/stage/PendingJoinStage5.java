package io.tatagulov.saveql.api.stage;

import io.tatagulov.saveql.query.JoinType;
import io.tatagulov.saveql.query.Query;
import io.tatagulov.saveql.schema.S1;
import io.tatagulov.saveql.schema.S2;
import io.tatagulov.saveql.schema.S3;
import io.tatagulov.saveql.schema.S4;
import io.tatagulov.saveql.schema.S5;
import io.tatagulov.saveql.schema.TableLike1;
import io.tatagulov.saveql.schema.TableLike2;
import io.tatagulov.saveql.schema.TableLike3;
import io.tatagulov.saveql.schema.TableLike4;
import io.tatagulov.saveql.schema.TableLike5;

final class PendingJoinStage5<Root, Left, Right, T1, T2, T3, T4, T5, PrevStage> extends PendingJoinStage<Root, Left, Right, S5, PrevStage>
        implements JoinStage5<Root, Left, Right, T1, T2, T3, T4, T5, PrevStage> {

    PendingJoinStage5(Query<Root> query, PrevStage previousStage, JoinType joinType, TableLike5<Right, T1, T2, T3, T4, T5> table) {
        super(query, previousStage, joinType, table);
    }

    public <R, N1> JoinStage1<Root, Right, R, N1, JoinStage5<Root, Left, Right, T1, T2, T3, T4, T5, PrevStage>> join(TableLike1<R, N1> table) {
        return new PendingJoinStage1<>(query, this, JoinType.INNER, table);
    }

    public <R, N1, N2> JoinStage2<Root, Right, R, N1, N2, JoinStage5<Root, Left, Right, T1, T2, T3, T4, T5, PrevStage>> join(TableLike2<R, N1, N2> table) {
        return new PendingJoinStage2<>(query, this, JoinType.INNER, table);
    }

    public <R, N1, N2, N3> JoinStage3<Root, Right, R, N1, N2, N3, JoinStage5<Root, Left, Right, T1, T2, T3, T4, T5, PrevStage>> join(TableLike3<R, N1, N2, N3> table) {
        return new PendingJoinStage3<>(query, this, JoinType.INNER, table);
    }

    public <R, N1, N2, N3, N4> JoinStage4<Root, Right, R, N1, N2, N3, N4, JoinStage5<Root, Left, Right, T1, T2, T3, T4, T5, PrevStage>> join(TableLike4<R, N1, N2, N3, N4> table) {
        return new PendingJoinStage4<>(query, this, JoinType.INNER, table);
    }

    public <R, N1, N2, N3, N4, N5> JoinStage5<Root, Right, R, N1, N2, N3, N4, N5, JoinStage5<Root, Left, Right, T1, T2, T3, T4, T5, PrevStage>> join(
            TableLike5<R, N1, N2, N3, N4, N5> table
    ) {
        return new PendingJoinStage5<>(query, this, JoinType.INNER, table);
    }

    public <R, N1> JoinStage1<Root, Right, R, N1, JoinStage5<Root, Left, Right, T1, T2, T3, T4, T5, PrevStage>> leftJoin(TableLike1<R, N1> table) {
        return new PendingJoinStage1<>(query, this, JoinType.LEFT, table);
    }

    public <R, N1, N2> JoinStage2<Root, Right, R, N1, N2, JoinStage5<Root, Left, Right, T1, T2, T3, T4, T5, PrevStage>> leftJoin(TableLike2<R, N1, N2> table) {
        return new PendingJoinStage2<>(query, this, JoinType.LEFT, table);
    }

    public <R, N1, N2, N3> JoinStage3<Root, Right, R, N1, N2, N3, JoinStage5<Root, Left, Right, T1, T2, T3, T4, T5, PrevStage>> leftJoin(TableLike3<R, N1, N2, N3> table) {
        return new PendingJoinStage3<>(query, this, JoinType.LEFT, table);
    }

    public <R, N1, N2, N3, N4> JoinStage4<Root, Right, R, N1, N2, N3, N4, JoinStage5<Root, Left, Right, T1, T2, T3, T4, T5, PrevStage>> leftJoin(TableLike4<R, N1, N2, N3, N4> table) {
        return new PendingJoinStage4<>(query, this, JoinType.LEFT, table);
    }

    public <R, N1, N2, N3, N4, N5> JoinStage5<Root, Right, R, N1, N2, N3, N4, N5, JoinStage5<Root, Left, Right, T1, T2, T3, T4, T5, PrevStage>> leftJoin(
            TableLike5<R, N1, N2, N3, N4, N5> table
    ) {
        return new PendingJoinStage5<>(query, this, JoinType.LEFT, table);
    }
}
