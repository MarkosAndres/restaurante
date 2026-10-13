package com.restaurant.pos.shift;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OpenShiftRequest(
        @NotNull @DecimalMin("0") BigDecimal openingCash)
{
    public OpenShiftRequest(BigDecimal openingCash)
    {
        this.openingCash = openingCash;
    }
}
