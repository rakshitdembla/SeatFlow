package com.rakshitdembla.event_ticket_booking.mapper;

import com.rakshitdembla.event_ticket_booking.dto.response.EventDetailResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.EventSummaryResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.SeatCategoryResponse;
import com.rakshitdembla.event_ticket_booking.entity.Event;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventMapper {

    public EventSummaryResponse toSummaryResponse(Event event) {
        return EventSummaryResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .venueName(event.getVenueName())
                .city(event.getCity())
                .state(event.getState())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .status(event.getStatus())
                .build();
    }

    public EventDetailResponse toDetailResponse(Event event, List<SeatCategoryResponse> seatCategories) {
        return EventDetailResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .venueName(event.getVenueName())
                .address(event.getAddress())
                .city(event.getCity())
                .state(event.getState())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .bookingStart(event.getBookingStart())
                .bookingEnd(event.getBookingEnd())
                .status(event.getStatus())
                .createdByName(event.getCreatedBy().getName())
                .seatCategories(seatCategories)
                .build();
    }
}
