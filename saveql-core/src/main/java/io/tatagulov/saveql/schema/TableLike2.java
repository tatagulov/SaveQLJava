package io.tatagulov.saveql.schema;

public interface TableLike2<R, T1, T2> extends TableLike<R, S2> {
    @Override
    Cols2<R, T1, T2> primaryKeyCols();
}
