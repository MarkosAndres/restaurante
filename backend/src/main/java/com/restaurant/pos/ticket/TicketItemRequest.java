package com.restaurant.pos.ticket;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

final class Item
{
    @NotNull
    public final Long productId;

    @Min(1)
    public final int quantity;

    Item(Long productId, int quantity)
    {
        this.productId = productId;
        this.quantity = quantity;
    }
}
