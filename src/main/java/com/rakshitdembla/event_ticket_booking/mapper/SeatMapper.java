package com.rakshitdembla.event_ticket_booking.mapper;

import com.rakshitdembla.event_ticket_booking.dto.response.SeatResponse;
import com.rakshitdembla.event_ticket_booking.entity.Seat;
import org.springframework.stereotype.Component;

@Component
public class SeatMapper {

    public SeatResponse toResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .seatCategoryName(seat.getSeatCategory().getName())
                .price(seat.getSeatCategory().getPrice())
                .status(seat.getStatus())
                .build();
    }
}
