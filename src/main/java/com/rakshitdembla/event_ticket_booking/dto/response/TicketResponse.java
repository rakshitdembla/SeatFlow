package com.rakshitdembla.event_ticket_booking.dto.response;

import com.rakshitdembla.event_ticket_booking.enums.TicketStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TicketResponse {

    private Long id;
    private String uniqueTicketCode;
    private String seatNumber;
    private String seatCategoryName;
    private BigDecimal ticketPrice;
    private TicketStatus status;
}
