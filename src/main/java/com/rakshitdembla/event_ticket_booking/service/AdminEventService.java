package com.rakshitdembla.event_ticket_booking.service;

import com.rakshitdembla.event_ticket_booking.dto.request.CreateEventRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.SeatCategoryRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.UpdateEventRequest;
import com.rakshitdembla.event_ticket_booking.dto.response.ApiMessageResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.EventDetailResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.EventSummaryResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.SeatCategoryResponse;
import com.rakshitdembla.event_ticket_booking.entity.Event;
import com.rakshitdembla.event_ticket_booking.entity.Seat;
import com.rakshitdembla.event_ticket_booking.entity.SeatCategory;
import com.rakshitdembla.event_ticket_booking.entity.User;
import com.rakshitdembla.event_ticket_booking.enums.EventStatus;
import com.rakshitdembla.event_ticket_booking.enums.SeatStatus;
import com.rakshitdembla.event_ticket_booking.exception.ResourceNotFoundException;
import com.rakshitdembla.event_ticket_booking.exception.UnauthorizedActionException;
import com.rakshitdembla.event_ticket_booking.mapper.EventMapper;
import com.rakshitdembla.event_ticket_booking.mapper.SeatCategoryMapper;
import com.rakshitdembla.event_ticket_booking.repository.EventRepository;
import com.rakshitdembla.event_ticket_booking.repository.SeatCategoryRepository;
import com.rakshitdembla.event_ticket_booking.repository.SeatRepository;
import com.rakshitdembla.event_ticket_booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEventService {

    private final EventRepository eventRepository;
    private final SeatCategoryRepository seatCategoryRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final SeatCategoryMapper seatCategoryMapper;

    @Transactional
    public EventDetailResponse createEvent(CreateEventRequest request, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .venueName(request.getVenueName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .bookingStart(request.getBookingStart())
                .bookingEnd(request.getBookingEnd())
                .status(EventStatus.UPCOMING)
                .createdBy(admin)
                .build();

        eventRepository.save(event);

        List<SeatCategoryResponse> seatCategoryResponses = new ArrayList<>();

        for (SeatCategoryRequest categoryRequest : request.getSeatCategories()) {
            SeatCategory category = SeatCategory.builder()
                    .event(event)
                    .name(categoryRequest.getName())
                    .description(categoryRequest.getDescription())
                    .price(categoryRequest.getPrice())
                    .build();

            seatCategoryRepository.save(category);

            List<Seat> seats = new ArrayList<>();
            for (int i = 1; i <= categoryRequest.getTotalSeats(); i++) {
                seats.add(Seat.builder()
                        .event(event)
                        .seatCategory(category)
                        .seatNumber(categoryRequest.getName().toUpperCase() + "-" + i)
                        .status(SeatStatus.AVAILABLE)
                        .build());
            }
            seatRepository.saveAll(seats);

            seatCategoryResponses.add(seatCategoryMapper.toResponse(category, seats.size(), seats.size()));
        }

        return eventMapper.toDetailResponse(event, seatCategoryResponses);
    }

    @Transactional
    public EventDetailResponse updateEvent(Long eventId, UpdateEventRequest request, Long adminId) {
        Event event = getOwnedEvent(eventId, adminId);

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setVenueName(request.getVenueName());
        event.setAddress(request.getAddress());
        event.setCity(request.getCity());
        event.setState(request.getState());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setBookingStart(request.getBookingStart());
        event.setBookingEnd(request.getBookingEnd());
        event.setStatus(request.getStatus());

        eventRepository.save(event);

        List<SeatCategoryResponse> seatCategoryResponses = seatCategoryRepository.findByEventId(eventId).stream()
                .map(category -> seatCategoryMapper.toResponse(
                        category,
                        seatRepository.countBySeatCategoryId(category.getId()),
                        seatRepository.countBySeatCategoryIdAndStatus(category.getId(), SeatStatus.AVAILABLE)))
                .toList();

        return eventMapper.toDetailResponse(event, seatCategoryResponses);
    }

    @Transactional
    public ApiMessageResponse deleteEvent(Long eventId, Long adminId) {
        Event event = getOwnedEvent(eventId, adminId);

        seatRepository.deleteByEventId(event.getId());
        seatCategoryRepository.deleteByEventId(event.getId());
        eventRepository.delete(event);

        return new ApiMessageResponse("Event deleted successfully.");
    }

    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> getOwnEvents(Long adminId, Pageable pageable) {
        return eventRepository.findByCreatedById(adminId, pageable).map(eventMapper::toSummaryResponse);
    }

    private Event getOwnedEvent(Long eventId, Long adminId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found."));

        if (!event.getCreatedBy().getId().equals(adminId)) {
            throw new UnauthorizedActionException("You can only manage events you created.");
        }

        return event;
    }
}
