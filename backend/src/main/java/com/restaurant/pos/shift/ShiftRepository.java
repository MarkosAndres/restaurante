package com.restaurant.pos.shift;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, Long>
{
    Optional<Shift> findByWaiterIdAndStatus(String waiterId, String status);

    List<Shift> findAllByOrderByOpenedAtDesc();
}
