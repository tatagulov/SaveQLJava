package io.tatagulov.saveql.schema;

public abstract class TableDef4<R, T1, T2, T3, T4> extends TableDef<R, S4> implements TableLike4<R, T1, T2, T3, T4> {
    protected TableDef4(String name, String alias) {
        super(name, alias);
    }

    @Override
    public abstract Cols4<R, T1, T2, T3, T4> primaryKeyCols();
}
