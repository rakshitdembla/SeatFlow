package com.rakshitdembla.event_ticket_booking.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BookedSeatResponse {

    private Long seatId;
    private String seatNumber;
    private String seatCategoryName;
    private BigDecimal price;
}
