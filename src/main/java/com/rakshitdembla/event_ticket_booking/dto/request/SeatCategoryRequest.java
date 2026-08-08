package com.rakshitdembla.event_ticket_booking.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatCategoryRequest {

    @NotBlank(message = "Seat category name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Total seats is required")
    @Positive(message = "Total seats must be greater than zero")
    @Max(value = 5000, message = "Total seats per category cannot exceed 5000")
    private Integer totalSeats;
}
