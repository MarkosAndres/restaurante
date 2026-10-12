package com.restaurant.pos.ticket;

import jakarta.validation.constraints.NotBlank;

final class Pay
{
    @NotBlank
    public final String idempotencyKey;

    Pay(String idempotencyKey)
    {
        this.idempotencyKey = idempotencyKey;
    }
}
