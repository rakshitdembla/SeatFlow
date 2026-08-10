package com.rakshitdembla.event_ticket_booking.dto.response;

import com.rakshitdembla.event_ticket_booking.enums.BookingStatus;
import com.rakshitdembla.event_ticket_booking.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BookingResponse {

    private Long id;
    private String bookingReference;
    private Long eventId;
    private String eventTitle;
    private BigDecimal totalAmount;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private LocalDateTime bookedAt;
    private LocalDateTime seatLockExpiresAt;
    private List<BookedSeatResponse> seats;
}
