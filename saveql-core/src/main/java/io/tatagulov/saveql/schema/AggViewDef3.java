package io.tatagulov.saveql.schema;

public abstract class AggViewDef3<VR, SR, T1, T2, T3> extends AggViewDef<VR, SR, S3> implements TableLike3<VR, T1, T2, T3> {
    protected AggViewDef3(String alias, Cols3<SR, T1, T2, T3> primaryKeyCols) {
        super(alias, primaryKeyCols);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Cols3<VR, T1, T2, T3> primaryKeyCols() {
        return (Cols3<VR, T1, T2, T3>) super.primaryKeyCols();
    }
}
