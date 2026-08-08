package com.rakshitdembla.event_ticket_booking.service;

import com.rakshitdembla.event_ticket_booking.dto.response.EventDetailResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.EventSummaryResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.SeatCategoryResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.SeatResponse;
import com.rakshitdembla.event_ticket_booking.entity.Event;
import com.rakshitdembla.event_ticket_booking.enums.SeatStatus;
import com.rakshitdembla.event_ticket_booking.exception.ResourceNotFoundException;
import com.rakshitdembla.event_ticket_booking.mapper.EventMapper;
import com.rakshitdembla.event_ticket_booking.mapper.SeatCategoryMapper;
import com.rakshitdembla.event_ticket_booking.mapper.SeatMapper;
import com.rakshitdembla.event_ticket_booking.repository.EventRepository;
import com.rakshitdembla.event_ticket_booking.repository.SeatCategoryRepository;
import com.rakshitdembla.event_ticket_booking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final SeatCategoryRepository seatCategoryRepository;
    private final SeatRepository seatRepository;
    private final EventMapper eventMapper;
    private final SeatCategoryMapper seatCategoryMapper;
    private final SeatMapper seatMapper;

    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> getEvents(Pageable pageable) {
        return eventRepository.findAll(pageable).map(eventMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found."));

        List<SeatCategoryResponse> seatCategoryResponses = seatCategoryRepository.findByEventId(eventId).stream()
                .map(category -> seatCategoryMapper.toResponse(
                        category,
                        seatRepository.countBySeatCategoryId(category.getId()),
                        seatRepository.countBySeatCategoryIdAndStatus(category.getId(), SeatStatus.AVAILABLE)))
                .toList();

        return eventMapper.toDetailResponse(event, seatCategoryResponses);
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getEventSeats(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found.");
        }

        return seatRepository.findByEventIdOrderBySeatNumberAsc(eventId).stream()
                .map(seatMapper::toResponse)
                .toList();
    }
}
