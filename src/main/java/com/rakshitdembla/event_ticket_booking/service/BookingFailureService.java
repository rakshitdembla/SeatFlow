package com.rakshitdembla.event_ticket_booking.service;

import com.rakshitdembla.event_ticket_booking.entity.Booking;
import com.rakshitdembla.event_ticket_booking.entity.Payment;
import com.rakshitdembla.event_ticket_booking.entity.Seat;
import com.rakshitdembla.event_ticket_booking.enums.BookingStatus;
import com.rakshitdembla.event_ticket_booking.enums.PaymentStatus;
import com.rakshitdembla.event_ticket_booking.enums.SeatStatus;
import com.rakshitdembla.event_ticket_booking.repository.BookingRepository;
import com.rakshitdembla.event_ticket_booking.repository.PaymentRepository;
import com.rakshitdembla.event_ticket_booking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Persists a booking/payment/seat failure in its own transaction (REQUIRES_NEW),
 * independent of whatever caused the failure. This matters specifically because
 * callers (see PaymentService) invoke this and then throw, and the calling
 * transaction rolling back must not also undo the failure state itself.
 */
@Service
@RequiredArgsConstructor
public class BookingFailureService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final SeatRepository seatRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failBooking(Booking booking, List<Seat> seats, Payment payment) {
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        booking.setBookingStatus(BookingStatus.FAILED);
        bookingRepository.save(booking);

        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedBy(null);
            seat.setLockedUntil(null);
            seat.setBooking(null);
        }
        seatRepository.saveAll(seats);
    }
}
