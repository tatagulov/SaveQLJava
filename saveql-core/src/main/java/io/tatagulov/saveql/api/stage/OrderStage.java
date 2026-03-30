package io.tatagulov.saveql.api.stage;

import io.tatagulov.saveql.query.OrderItem;

public interface OrderStage<L> extends LimitStage<L> {
    LimitStage<L> orderBy(OrderItem... items);
}
