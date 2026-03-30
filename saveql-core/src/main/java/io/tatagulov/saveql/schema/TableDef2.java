package io.tatagulov.saveql.schema;

public abstract class TableDef2<R, T1, T2> extends TableDef<R, S2> implements TableLike2<R, T1, T2> {
    protected TableDef2(String name, String alias) {
        super(name, alias);
    }

    @Override
    public abstract Cols2<R, T1, T2> primaryKeyCols();
}
