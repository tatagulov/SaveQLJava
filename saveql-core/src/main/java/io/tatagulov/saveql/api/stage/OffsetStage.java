package io.tatagulov.saveql.api.stage;

public interface OffsetStage<L> extends SelectStage<L> {
    SelectStage<L> offset(int offset);
}
