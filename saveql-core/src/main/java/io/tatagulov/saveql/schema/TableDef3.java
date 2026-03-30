package io.tatagulov.saveql.schema;

public abstract class TableDef3<R, T1, T2, T3> extends TableDef<R, S3> implements TableLike3<R, T1, T2, T3> {
    protected TableDef3(String name, String alias) {
        super(name, alias);
    }

    @Override
    public abstract Cols3<R, T1, T2, T3> primaryKeyCols();
}
