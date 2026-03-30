package io.tatagulov.saveql.schema;

public interface TableLike1<R, T1> extends TableLike<R, S1> {
    @Override
    Cols1<R, T1> primaryKeyCols();
}
