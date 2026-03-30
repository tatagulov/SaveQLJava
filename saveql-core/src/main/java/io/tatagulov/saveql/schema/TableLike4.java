package io.tatagulov.saveql.schema;

public interface TableLike4<R, T1, T2, T3, T4> extends TableLike<R, S4> {
    @Override
    Cols4<R, T1, T2, T3, T4> primaryKeyCols();
}
