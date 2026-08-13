package com.rakshitdembla.event_ticket_booking.scheduler;

import com.rakshitdembla.event_ticket_booking.entity.Booking;
import com.rakshitdembla.event_ticket_booking.entity.Payment;
import com.rakshitdembla.event_ticket_booking.entity.Seat;
import com.rakshitdembla.event_ticket_booking.enums.BookingStatus;
import com.rakshitdembla.event_ticket_booking.enums.SeatStatus;
import com.rakshitdembla.event_ticket_booking.repository.PaymentRepository;
import com.rakshitdembla.event_ticket_booking.repository.SeatRepository;
import com.rakshitdembla.event_ticket_booking.service.BookingFailureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatUnlockScheduler {

    private final SeatRepository seatRepository;
    private final PaymentRepository paymentRepository;
    private final BookingFailureService bookingFailureService;

    @Scheduled(fixedRate = 60000)
    @Transactional(readOnly = true)
    public void releaseExpiredSeatLocks() {
        List<Seat> expiredSeats = seatRepository.findByStatusAndLockedUntilBefore(SeatStatus.LOCKED, LocalDateTime.now());

        if (expiredSeats.isEmpty()) {
            return;
        }

        Map<Long, List<Seat>> seatsByBookingId = expiredSeats.stream()
                .filter(seat -> seat.getBooking() != null)
                .collect(Collectors.groupingBy(seat -> seat.getBooking().getId()));

        for (List<Seat> seats : seatsByBookingId.values()) {
            Booking booking = seats.get(0).getBooking();

            if (booking.getBookingStatus() != BookingStatus.PENDING) {
                continue;
            }

            Payment payment = paymentRepository.findByBookingId(booking.getId()).orElse(null);
            if (payment == null) {
                continue;
            }

            bookingFailureService.failBooking(booking, seats, payment);
            log.info("Released {} expired seat lock(s) for booking {}", seats.size(), booking.getBookingReference());
        }
    }
}
