package io.tatagulov.saveql.api.stage;

import io.tatagulov.saveql.schema.*;

public interface JoinStage4<Root, Left, Right, T1, T2, T3, T4, PrevStage> extends JoinStage<Root, Left, Right, S4, PrevStage> {
    <R, N1> JoinStage1<Root, Right, R, N1, JoinStage4<Root, Left, Right, T1, T2, T3, T4, PrevStage>> join(TableLike1<R, N1> table);

    <R, N1, N2> JoinStage2<Root, Right, R, N1, N2, JoinStage4<Root, Left, Right, T1, T2, T3, T4, PrevStage>> join(TableLike2<R, N1, N2> table);

    <R, N1, N2, N3> JoinStage3<Root, Right, R, N1, N2, N3, JoinStage4<Root, Left, Right, T1, T2, T3, T4, PrevStage>> join(TableLike3<R, N1, N2, N3> table);

    <R, N1, N2, N3, N4> JoinStage4<Root, Right, R, N1, N2, N3, N4, JoinStage4<Root, Left, Right, T1, T2, T3, T4, PrevStage>> join(TableLike4<R, N1, N2, N3, N4> table);

    <R, N1, N2, N3, N4, N5> JoinStage5<Root, Right, R, N1, N2, N3, N4, N5, JoinStage4<Root, Left, Right, T1, T2, T3, T4, PrevStage>> join(
            TableLike5<R, N1, N2, N3, N4, N5> table
    );

    <R, N1> JoinStage1<Root, Right, R, N1, JoinStage4<Root, Left, Right, T1, T2, T3, T4, PrevStage>> leftJoin(TableLike1<R, N1> table);

    <R, N1, N2> JoinStage2<Root, Right, R, N1, N2, JoinStage4<Root, Left, Right, T1, T2, T3, T4, PrevStage>> leftJoin(TableLike2<R, N1, N2> table);

    <R, N1, N2, N3> JoinStage3<Root, Right, R, N1, N2, N3, JoinStage4<Root, Left, Right, T1, T2, T3, T4, PrevStage>> leftJoin(TableLike3<R, N1, N2, N3> table);

    <R, N1, N2, N3, N4> JoinStage4<Root, Right, R, N1, N2, N3, N4, JoinStage4<Root, Left, Right, T1, T2, T3, T4, PrevStage>> leftJoin(TableLike4<R, N1, N2, N3, N4> table);

    <R, N1, N2, N3, N4, N5> JoinStage5<Root, Right, R, N1, N2, N3, N4, N5, JoinStage4<Root, Left, Right, T1, T2, T3, T4, PrevStage>> leftJoin(
            TableLike5<R, N1, N2, N3, N4, N5> table
    );

    default PrevStage by(ColumnLike<Left, T1> c1, ColumnLike<Left, T2> c2, ColumnLike<Left, T3> c3, ColumnLike<Left, T4> c4) {
        return by(Cols.of(c1, c2, c3, c4));
    }
}
