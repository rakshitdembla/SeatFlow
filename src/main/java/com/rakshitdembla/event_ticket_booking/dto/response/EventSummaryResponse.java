package com.rakshitdembla.event_ticket_booking.dto.response;

import com.rakshitdembla.event_ticket_booking.enums.EventStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EventSummaryResponse {

    private Long id;
    private String title;
    private String venueName;
    private String city;
    private String state;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private EventStatus status;
}
