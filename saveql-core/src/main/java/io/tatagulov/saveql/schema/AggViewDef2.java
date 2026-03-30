package io.tatagulov.saveql.schema;

public abstract class AggViewDef2<VR, SR, T1, T2> extends AggViewDef<VR, SR, S2> implements TableLike2<VR, T1, T2> {
    protected AggViewDef2(String alias, Cols2<SR, T1, T2> primaryKeyCols) {
        super(alias, primaryKeyCols);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Cols2<VR, T1, T2> primaryKeyCols() {
        return (Cols2<VR, T1, T2>) super.primaryKeyCols();
    }
}
