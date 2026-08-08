package com.rakshitdembla.event_ticket_booking.repository;

import com.rakshitdembla.event_ticket_booking.entity.Seat;
import com.rakshitdembla.event_ticket_booking.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByEventIdOrderBySeatNumberAsc(Long eventId);

    long countBySeatCategoryId(Long seatCategoryId);

    long countBySeatCategoryIdAndStatus(Long seatCategoryId, SeatStatus status);

    void deleteByEventId(Long eventId);
}
