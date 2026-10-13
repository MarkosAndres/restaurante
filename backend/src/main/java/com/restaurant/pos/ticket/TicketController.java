package com.restaurant.pos.ticket;

import com.restaurant.pos.catalog.*;
import com.restaurant.pos.shift.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.*;

import java.math.*;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController
{
    final TicketRepository tickets;
    final CatalogRepository products;
    final ShiftRepository shifts;
    final JdbcTemplate jdbc;

    TicketController(
            TicketRepository ticketRepository,
            CatalogRepository catalogRepository,
            ShiftRepository shiftRepository,
            JdbcTemplate jdbcTemplate)
    {
        tickets = ticketRepository;
        products = catalogRepository;
        shifts = shiftRepository;
        jdbc = jdbcTemplate;
    }

    private String uid(Jwt j)
    {
        return j.getSubject();
    }

    private String uname(Jwt j)
    {
        var name = j.getClaimAsString("name");
        var preferredUsername = j.getClaimAsString("preferred_username");
        return (name == null)
                        ? preferredUsername
                        : name;
    }

    @GetMapping
    public List<Ticket> list()
    {
        return tickets.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id)
    {
        var ticket = tickets
                .findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("Ticket not found"));
        return Map.of(
                "ticket", ticket,
                "items", jdbc.queryForList("select * from ticket_items where ticket_id=?", id));
    }

    @PostMapping
    @Transactional
    public Ticket create(
            @AuthenticationPrincipal Jwt jsonWebToken,
            @Valid @RequestBody CreateTicketRequest createRequest)
    {
        var shift = shifts
                .findByWaiterIdAndStatus(uid(jsonWebToken), "OPEN")
                .orElseThrow(() -> new IllegalStateException("An active shift is required"));
        var ticket = new Ticket();
        ticket.ticketNumber = "T-" + System.currentTimeMillis();
        ticket.waiterId = uid(jsonWebToken);
        ticket.waiterName = uname(jsonWebToken);
        ticket.shiftId = shift.id;
        ticket.tableNumber = createRequest.tableNumber();
        ticket.status = "OPEN";
        ticket.createdAt = Instant.now();
        ticket.subtotal = BigDecimal.ZERO;

        for (var item : createRequest.ticketItemRequests())
        {
            var product = products
                    .findById(item.productId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Product not found"));
            if (!product.available)
                throw new IllegalStateException("Product unavailable");
            var line = product.price.multiply(BigDecimal.valueOf(item.quantity()));
            ticket.subtotal = ticket.subtotal.add(line);
        }

        ticket.tipAmount = BigDecimal.ZERO;
        ticket.total = ticket.subtotal;
        ticket = tickets.save(ticket);

        for (var item : createRequest.ticketItemRequests())
        {
            var product = products
                    .findById(item.productId())
                    .orElseThrow();
            var line = product.price.multiply(BigDecimal.valueOf(item.quantity()));
            jdbc.update(
                    "insert into ticket_items(ticket_id,product_id,name,quantity,unit_price,line_total) values(?,?,?,?,?,?)",
                    ticket.id,
                    product.id,
                    product.name,
                    item.quantity(),
                    product.price,
                    line);
        }
        return ticket;
    }

    @PutMapping("/{id}/tip")
    @Transactional
    public Ticket tip(
            @PathVariable Long id,
            @Valid @RequestBody TicketTipRequest ticketTipRequestRequest)
    {
        var ticket = open(id);
        BigDecimal value =
                ticketTipRequestRequest.value() == null
                        ? BigDecimal.ZERO
                        : ticketTipRequestRequest.value();
        ticket.tipAmount =
                switch (ticketTipRequestRequest.mode() == null
                            ? "NONE"
                            : ticketTipRequestRequest.mode())
                {
                    case "PERCENT" -> ticket
                            .subtotal
                            .multiply(value)
                            .divide(BigDecimal.valueOf(100));
                    case "FIXED" -> value;
                    default -> BigDecimal.ZERO;
                };

        if (ticket.tipAmount.signum() < 0)
            throw new IllegalArgumentException("Tip cannot be negative");

        ticket.tipAmount = ticket
                .tipAmount
                .setScale(2, RoundingMode.HALF_UP);
        ticket.total = ticket.subtotal.add(ticket.tipAmount);
        return tickets.save(ticket);
    }

    @PostMapping("/{id}/payment/cash")
    @Transactional
    public Ticket pay(
            @PathVariable Long id,
            @Valid @RequestBody CashPaymentRequest cashPaymentRequest)
    {
        var exists = jdbc.queryForObject(
                "select count(*) from ticket_payments where idempotency_key=?",
                Integer.class,
                cashPaymentRequest.idempotencyKey());

        if (exists != null && exists > 0)
            return tickets.findById(id).orElseThrow();

        var ticket = open(id);
        jdbc.update(
                "insert into ticket_payments(ticket_id,shift_id,amount,method,idempotency_key) values(?,?,?,?,?)",
                ticket.id,
                ticket.shiftId,
                ticket.total,
                "CASH",
                cashPaymentRequest.idempotencyKey());

        ticket.status = "PAID";
        ticket.paidAt = Instant.now();
        return tickets.save(ticket);
    }

    private Ticket open(Long id)
    {
        var ticket = tickets
                .findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("Ticket not found"));

        if (!"OPEN".equals(ticket.status))
            throw new IllegalStateException("Only open tickets can be modified");

        return ticket;
    }
}
