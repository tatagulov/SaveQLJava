package io.tatagulov.saveql.schema;

public abstract class AggViewDef5<VR, SR, T1, T2, T3, T4, T5> extends AggViewDef<VR, SR, S5> implements TableLike5<VR, T1, T2, T3, T4, T5> {
    protected AggViewDef5(String alias, Cols5<SR, T1, T2, T3, T4, T5> primaryKeyCols) {
        super(alias, primaryKeyCols);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Cols5<VR, T1, T2, T3, T4, T5> primaryKeyCols() {
        return (Cols5<VR, T1, T2, T3, T4, T5>) super.primaryKeyCols();
    }
}
