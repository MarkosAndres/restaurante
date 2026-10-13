package com.restaurant.pos.ticket;

import java.math.BigDecimal;

record TicketTipRequest(String mode, BigDecimal value)
{
}
