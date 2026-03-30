package io.tatagulov.saveql.schema;

public abstract class AggViewDef1<VR, SR, T1> extends AggViewDef<VR, SR, S1> implements TableLike1<VR, T1> {
    protected AggViewDef1(String alias, Cols1<SR, T1> primaryKeyCols) {
        super(alias, primaryKeyCols);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Cols1<VR, T1> primaryKeyCols() {
        return (Cols1<VR, T1>) super.primaryKeyCols();
    }
}
