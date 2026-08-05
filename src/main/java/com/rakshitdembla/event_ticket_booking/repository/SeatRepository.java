package com.rakshitdembla.event_ticket_booking.repository;

import com.rakshitdembla.event_ticket_booking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByEventId(Long eventId);
}
