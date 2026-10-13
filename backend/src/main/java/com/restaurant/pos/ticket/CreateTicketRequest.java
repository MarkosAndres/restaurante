package com.restaurant.pos.ticket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

record CreateTicketRequest(
        String tableNumber,
        @NotEmpty List<@Valid TicketItemRequest> ticketItemRequests)
{
    CreateTicketRequest(
            String tableNumber,
            List<TicketItemRequest> ticketItemRequests)
    {
        this.tableNumber = tableNumber;
        this.ticketItemRequests = ticketItemRequests;
    }
}
