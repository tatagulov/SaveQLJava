package io.tatagulov.saveql.api.stage;

public interface LimitStage<L> extends OffsetStage<L> {
    OffsetStage<L> limit(int limit);
}
