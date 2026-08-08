package com.rakshitdembla.event_ticket_booking.dto.request;

import com.rakshitdembla.event_ticket_booking.enums.EventStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Venue name is required")
    private String venueName;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @NotNull(message = "Booking start time is required")
    private LocalDateTime bookingStart;

    @NotNull(message = "Booking end time is required")
    private LocalDateTime bookingEnd;

    @NotNull(message = "Status is required")
    private EventStatus status;

    @AssertTrue(message = "End time must be after start time")
    private boolean isEndTimeValid() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }

    @AssertTrue(message = "Booking end time must be before the event start time")
    private boolean isBookingEndValid() {
        return bookingEnd == null || startTime == null || !bookingEnd.isAfter(startTime);
    }

    @AssertTrue(message = "Booking start time must be before booking end time")
    private boolean isBookingWindowValid() {
        return bookingStart == null || bookingEnd == null || bookingStart.isBefore(bookingEnd);
    }
}
