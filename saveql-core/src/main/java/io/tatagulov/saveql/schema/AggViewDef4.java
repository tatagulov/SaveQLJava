package io.tatagulov.saveql.schema;

public abstract class AggViewDef4<VR, SR, T1, T2, T3, T4> extends AggViewDef<VR, SR, S4> implements TableLike4<VR, T1, T2, T3, T4> {
    protected AggViewDef4(String alias, Cols4<SR, T1, T2, T3, T4> primaryKeyCols) {
        super(alias, primaryKeyCols);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Cols4<VR, T1, T2, T3, T4> primaryKeyCols() {
        return (Cols4<VR, T1, T2, T3, T4>) super.primaryKeyCols();
    }
}
