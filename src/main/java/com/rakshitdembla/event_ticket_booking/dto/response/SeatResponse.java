package com.rakshitdembla.event_ticket_booking.dto.response;

import com.rakshitdembla.event_ticket_booking.enums.SeatStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SeatResponse {

    private Long id;
    private String seatNumber;
    private String seatCategoryName;
    private BigDecimal price;
    private SeatStatus status;
}
