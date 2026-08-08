package com.rakshitdembla.event_ticket_booking.mapper;

import com.rakshitdembla.event_ticket_booking.dto.response.SeatCategoryResponse;
import com.rakshitdembla.event_ticket_booking.entity.SeatCategory;
import org.springframework.stereotype.Component;

@Component
public class SeatCategoryMapper {

    public SeatCategoryResponse toResponse(SeatCategory category, long totalSeats, long availableSeats) {
        return SeatCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .price(category.getPrice())
                .totalSeats(totalSeats)
                .availableSeats(availableSeats)
                .build();
    }
}
