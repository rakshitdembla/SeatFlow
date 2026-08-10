package com.rakshitdembla.event_ticket_booking.controller;

import com.rakshitdembla.event_ticket_booking.dto.request.CreateBookingRequest;
import com.rakshitdembla.event_ticket_booking.dto.response.BookingResponse;
import com.rakshitdembla.event_ticket_booking.security.UserPrincipal;
import com.rakshitdembla.event_ticket_booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request, principal.getId()));
    }

    @GetMapping
    public ResponseEntity<Page<BookingResponse>> getOwnBookings(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "bookedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(bookingService.getOwnBookings(principal.getId(), pageable));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getOwnBookingDetail(@PathVariable Long bookingId,
                                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bookingService.getOwnBooking(bookingId, principal.getId()));
    }
}
