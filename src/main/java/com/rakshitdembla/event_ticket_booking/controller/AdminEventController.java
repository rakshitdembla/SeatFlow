package com.rakshitdembla.event_ticket_booking.controller;

import com.rakshitdembla.event_ticket_booking.dto.request.CreateEventRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.UpdateEventRequest;
import com.rakshitdembla.event_ticket_booking.dto.response.ApiMessageResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.EventDetailResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.EventSummaryResponse;
import com.rakshitdembla.event_ticket_booking.security.UserPrincipal;
import com.rakshitdembla.event_ticket_booking.service.AdminEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEventController {

    private final AdminEventService adminEventService;

    @PostMapping
    public ResponseEntity<EventDetailResponse> createEvent(@Valid @RequestBody CreateEventRequest request,
                                                             @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminEventService.createEvent(request, principal.getId()));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventDetailResponse> updateEvent(@PathVariable Long eventId,
                                                             @Valid @RequestBody UpdateEventRequest request,
                                                             @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(adminEventService.updateEvent(eventId, request, principal.getId()));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<ApiMessageResponse> deleteEvent(@PathVariable Long eventId,
                                                            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(adminEventService.deleteEvent(eventId, principal.getId()));
    }

    @GetMapping
    public ResponseEntity<Page<EventSummaryResponse>> getOwnEvents(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminEventService.getOwnEvents(principal.getId(), pageable));
    }
}
