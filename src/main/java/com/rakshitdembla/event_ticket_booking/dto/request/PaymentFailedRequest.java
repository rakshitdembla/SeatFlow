package com.rakshitdembla.event_ticket_booking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;
}
