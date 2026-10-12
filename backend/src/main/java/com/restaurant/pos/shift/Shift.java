package com.restaurant.pos.shift;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "shifts")
public class Shift
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String waiterId;
    public String waiterName;
    public String status;
    public Instant openedAt;
    public Instant closedAt;
    public BigDecimal openingCash;
    public BigDecimal closingCash;
    public BigDecimal expectedCash;
    public BigDecimal cashDifference;
}
