package com.rakshitdembla.event_ticket_booking.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SeatCategoryResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private long totalSeats;
    private long availableSeats;
}
