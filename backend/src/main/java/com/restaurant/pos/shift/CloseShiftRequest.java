package com.restaurant.pos.shift;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CloseShiftRequest(
        @NotNull @DecimalMin("0") BigDecimal closingCash)
{
    public CloseShiftRequest(BigDecimal closingCash)
    {
        this.closingCash = closingCash;
    }
}
