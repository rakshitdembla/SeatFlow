package com.rakshitdembla.event_ticket_booking.repository;

import com.rakshitdembla.event_ticket_booking.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCreatedById(Long createdById);
}
