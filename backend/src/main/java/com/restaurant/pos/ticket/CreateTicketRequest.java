package com.restaurant.pos.ticket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

final class Create
{
    public final String tableNumber;

    @NotEmpty
    public final List<@Valid Item> items;

    Create(String tableNumber, List<Item> items)
    {
        this.tableNumber = tableNumber;
        this.items = items;
    }
}
