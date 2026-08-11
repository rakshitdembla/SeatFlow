package com.rakshitdembla.event_ticket_booking.mapper;

import com.rakshitdembla.event_ticket_booking.dto.response.BookedSeatResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.BookingResponse;
import com.rakshitdembla.event_ticket_booking.entity.Booking;
import com.rakshitdembla.event_ticket_booking.entity.Payment;
import com.rakshitdembla.event_ticket_booking.entity.Seat;
import com.rakshitdembla.event_ticket_booking.enums.BookingStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingMapper {

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    public BookedSeatResponse toSeatResponse(Seat seat) {
        return BookedSeatResponse.builder()
                .seatId(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .seatCategoryName(seat.getSeatCategory().getName())
                .price(seat.getSeatCategory().getPrice())
                .build();
    }

    public BookingResponse toResponse(Booking booking, List<Seat> seats, Payment payment) {
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
                .paymentStatus(payment != null ? payment.getStatus() : null)
                .bookedAt(booking.getBookedAt())
                .seatLockExpiresAt(lockExpiry)
                .razorpayOrderId(payment != null ? payment.getRazorpayOrderId() : null)
                .razorpayKeyId(booking.getBookingStatus() == BookingStatus.PENDING ? razorpayKeyId : null)
                .seats(seats.stream().map(this::toSeatResponse).toList())
                .build();
    }
}
