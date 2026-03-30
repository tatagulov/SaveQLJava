package io.tatagulov.saveql.schema;

public abstract class TableDef1<R, T1> extends TableDef<R, S1> implements TableLike1<R, T1> {
    protected TableDef1(String name, String alias) {
        super(name, alias);
    }

    @Override
    public abstract Cols1<R, T1> primaryKeyCols();
}
