package io.tatagulov.saveql.api.stage;

import io.tatagulov.saveql.schema.*;

public interface JoinStage2<Root, Left, Right, T1, T2, PrevStage> extends JoinStage<Root, Left, Right, S2, PrevStage> {
    <R, N1> JoinStage1<Root, Right, R, N1, JoinStage2<Root, Left, Right, T1, T2, PrevStage>> join(TableLike1<R, N1> table);

    <R, N1, N2> JoinStage2<Root, Right, R, N1, N2, JoinStage2<Root, Left, Right, T1, T2, PrevStage>> join(TableLike2<R, N1, N2> table);

    <R, N1, N2, N3> JoinStage3<Root, Right, R, N1, N2, N3, JoinStage2<Root, Left, Right, T1, T2, PrevStage>> join(TableLike3<R, N1, N2, N3> table);

    <R, N1, N2, N3, N4> JoinStage4<Root, Right, R, N1, N2, N3, N4, JoinStage2<Root, Left, Right, T1, T2, PrevStage>> join(TableLike4<R, N1, N2, N3, N4> table);

    <R, N1, N2, N3, N4, N5> JoinStage5<Root, Right, R, N1, N2, N3, N4, N5, JoinStage2<Root, Left, Right, T1, T2, PrevStage>> join(
            TableLike5<R, N1, N2, N3, N4, N5> table
    );

    <R, N1> JoinStage1<Root, Right, R, N1, JoinStage2<Root, Left, Right, T1, T2, PrevStage>> leftJoin(TableLike1<R, N1> table);

    <R, N1, N2> JoinStage2<Root, Right, R, N1, N2, JoinStage2<Root, Left, Right, T1, T2, PrevStage>> leftJoin(TableLike2<R, N1, N2> table);

    <R, N1, N2, N3> JoinStage3<Root, Right, R, N1, N2, N3, JoinStage2<Root, Left, Right, T1, T2, PrevStage>> leftJoin(TableLike3<R, N1, N2, N3> table);

    <R, N1, N2, N3, N4> JoinStage4<Root, Right, R, N1, N2, N3, N4, JoinStage2<Root, Left, Right, T1, T2, PrevStage>> leftJoin(TableLike4<R, N1, N2, N3, N4> table);

    <R, N1, N2, N3, N4, N5> JoinStage5<Root, Right, R, N1, N2, N3, N4, N5, JoinStage2<Root, Left, Right, T1, T2, PrevStage>> leftJoin(
            TableLike5<R, N1, N2, N3, N4, N5> table
    );

    default PrevStage by(ColumnLike<Left, T1> c1, ColumnLike<Left, T2> c2) {
        return by(Cols.of(c1, c2));
    }
}
