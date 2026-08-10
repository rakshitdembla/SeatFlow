package com.rakshitdembla.event_ticket_booking.mapper;

import com.rakshitdembla.event_ticket_booking.dto.response.BookedSeatResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.BookingResponse;
import com.rakshitdembla.event_ticket_booking.entity.Booking;
import com.rakshitdembla.event_ticket_booking.entity.Seat;
import com.rakshitdembla.event_ticket_booking.enums.BookingStatus;
import com.rakshitdembla.event_ticket_booking.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingMapper {

    public BookedSeatResponse toSeatResponse(Seat seat) {
        return BookedSeatResponse.builder()
                .seatId(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .seatCategoryName(seat.getSeatCategory().getName())
                .price(seat.getSeatCategory().getPrice())
                .build();
    }

    public BookingResponse toResponse(Booking booking, List<Seat> seats, PaymentStatus paymentStatus) {
        LocalDateTime lockExpiry = booking.getBookingStatus() == BookingStatus.PENDING && !seats.isEmpty()
                ? seats.get(0).getLockedUntil()
                : null;

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .eventId(booking.getEvent().getId())
                .eventTitle(booking.getEvent().getTitle())
                .totalAmount(booking.getTotalAmount())
                .bookingStatus(booking.getBookingStatus())
                .paymentStatus(paymentStatus)
                .bookedAt(booking.getBookedAt())
                .seatLockExpiresAt(lockExpiry)
                .seats(seats.stream().map(this::toSeatResponse).toList())
                .build();
    }
}
