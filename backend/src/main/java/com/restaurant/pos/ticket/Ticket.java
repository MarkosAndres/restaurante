package com.restaurant.pos.ticket;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tickets")
public class Ticket
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String ticketNumber;
    public String waiterId;
    public String waiterName;
    public String tableNumber;
    public String status;
    public Long shiftId;
    public BigDecimal subtotal;
    public BigDecimal tipAmount;
    public BigDecimal total;
    public Instant createdAt;
    public Instant paidAt;
}
