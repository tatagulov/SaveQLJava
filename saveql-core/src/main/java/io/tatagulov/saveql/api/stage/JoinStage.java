package io.tatagulov.saveql.api.stage;

import io.tatagulov.saveql.schema.Cols;
import io.tatagulov.saveql.schema.Shape;
import io.tatagulov.saveql.schema.TableLike;

public interface JoinStage<Root, Left, Right, S extends Shape, PrevStage> {
    <R, NextS extends Shape> JoinStage<Root, Right, R, NextS, JoinStage<Root, Left, Right, S, PrevStage>> join(TableLike<R, NextS> table);

    <R, NextS extends Shape> JoinStage<Root, Right, R, NextS, JoinStage<Root, Left, Right, S, PrevStage>> leftJoin(TableLike<R, NextS> table);

    PrevStage by(Cols<S, Left> cols);
}
