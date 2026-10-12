package com.restaurant.pos.shift;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class OpenShiftRequest
{
    @NotNull
    @DecimalMin("0")
    public final BigDecimal openingCash;

    public OpenShiftRequest(BigDecimal openingCash)
    {
        this.openingCash = openingCash;
    }
}
