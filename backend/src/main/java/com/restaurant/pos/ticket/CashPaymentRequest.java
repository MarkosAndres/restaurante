package com.restaurant.pos.ticket;

import jakarta.validation.constraints.NotBlank;

record CashPaymentRequest(@NotBlank String idempotencyKey)
{
    CashPaymentRequest(String idempotencyKey)
    {
        this.idempotencyKey = idempotencyKey;
    }
}
