package io.tatagulov.saveql.schema;

public interface TableLike3<R, T1, T2, T3> extends TableLike<R, S3> {
    @Override
    Cols3<R, T1, T2, T3> primaryKeyCols();
}
