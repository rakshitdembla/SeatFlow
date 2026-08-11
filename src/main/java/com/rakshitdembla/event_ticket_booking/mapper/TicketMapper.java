package com.rakshitdembla.event_ticket_booking.mapper;

import com.rakshitdembla.event_ticket_booking.dto.response.TicketResponse;
import com.rakshitdembla.event_ticket_booking.entity.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .uniqueTicketCode(ticket.getUniqueTicketCode())
                .seatNumber(ticket.getSeat().getSeatNumber())
                .seatCategoryName(ticket.getSeat().getSeatCategory().getName())
                .ticketPrice(ticket.getTicketPrice())
                .status(ticket.getStatus())
                .build();
    }
}
