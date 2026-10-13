package com.restaurant.pos.ticket;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

record TicketItemRequest(
        @NotNull Long productId,
        @Min(1) int quantity)
{
    TicketItemRequest(Long productId, int quantity)
    {
        this.productId = productId;
        this.quantity = quantity;
    }
}
