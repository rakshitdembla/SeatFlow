package com.rakshitdembla.event_ticket_booking.repository;

import com.rakshitdembla.event_ticket_booking.entity.SeatCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatCategoryRepository extends JpaRepository<SeatCategory, Long> {

    List<SeatCategory> findByEventId(Long eventId);

    void deleteByEventId(Long eventId);
}
