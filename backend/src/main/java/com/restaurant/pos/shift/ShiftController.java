package com.restaurant.pos.shift;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController
{
    final ShiftRepository shifts;
    final JdbcTemplate jdbc;

    ShiftController(ShiftRepository shiftRepository, JdbcTemplate jdbcTemplate)
    {
        shifts = shiftRepository;
        jdbc = jdbcTemplate;
    }

    private String id(Jwt jwt)
    {
        return jwt.getSubject();
    }

    private String name(Jwt jwt)
    {
        String name = jwt.getClaimAsString("name");
        return name == null ? jwt.getClaimAsString("preferred_username") : name;
    }

    @GetMapping("/current")
    public Shift current(@AuthenticationPrincipal Jwt jwt)
    {
        return shifts.findByWaiterIdAndStatus(id(jwt), "OPEN").orElse(null);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Shift> all()
    {
        return shifts.findAllByOrderByOpenedAtDesc();
    }

    @PostMapping("/open")
    @Transactional
    public Shift open(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody OpenShiftRequest request)
    {
        if (shifts.findByWaiterIdAndStatus(id(jwt), "OPEN").isPresent())
        {
            throw new IllegalStateException("An active shift already exists");
        }

        var shift = new Shift();
        shift.waiterId = id(jwt);
        shift.waiterName = name(jwt);
        shift.openingCash = request.openingCash;
        shift.status = "OPEN";
        shift.openedAt = Instant.now();
        return shifts.save(shift);
    }

    @PostMapping("/{id}/close")
    @Transactional
    public Shift close(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt,
                       @Valid @RequestBody CloseShiftRequest request)
    {
        var shift = shifts.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found"));
        if (!shift.waiterId.equals(this.id(jwt))
                && !jwt.getClaimAsMap("realm_access").toString().contains("ADMIN"))
        {
            throw new IllegalArgumentException("Not your shift");
        }

        Integer openTickets = jdbc.queryForObject(
                "select count(*) from tickets where shift_id=? and status='OPEN'", Integer.class, id);
        if (openTickets != null && openTickets > 0)
        {
            throw new IllegalStateException("Close or pay all tickets before closing shift");
        }

        BigDecimal payments = jdbc.queryForObject(
                "select coalesce(sum(amount),0) from ticket_payments where shift_id=?", BigDecimal.class, id);
        shift.expectedCash = shift.openingCash.add(payments);
        shift.closingCash = request.closingCash;
        shift.cashDifference = request.closingCash.subtract(shift.expectedCash);
        shift.closedAt = Instant.now();
        shift.status = "CLOSED";
        return shifts.save(shift);
    }
}
