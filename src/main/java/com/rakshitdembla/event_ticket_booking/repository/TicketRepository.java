package com.rakshitdembla.event_ticket_booking.repository;

import com.rakshitdembla.event_ticket_booking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByBookingIdOrderByIdAsc(Long bookingId);

    Optional<Ticket> findByUniqueTicketCode(String uniqueTicketCode);

    boolean existsByUniqueTicketCode(String uniqueTicketCode);
}
