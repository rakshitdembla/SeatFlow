package com.rakshitdembla.event_ticket_booking.dto.response;

import com.rakshitdembla.event_ticket_booking.enums.EventStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class EventDetailResponse {

    private Long id;
    private String title;
    private String description;
    private String venueName;
    private String address;
    private String city;
    private String state;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime bookingStart;
    private LocalDateTime bookingEnd;
    private EventStatus status;
    private String createdByName;
    private List<SeatCategoryResponse> seatCategories;
}
