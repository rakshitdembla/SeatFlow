package com.rakshitdembla.event_ticket_booking.service;

import com.rakshitdembla.event_ticket_booking.dto.request.CreateBookingRequest;
import com.rakshitdembla.event_ticket_booking.entity.Event;
import com.rakshitdembla.event_ticket_booking.entity.Seat;
import com.rakshitdembla.event_ticket_booking.entity.User;
import com.rakshitdembla.event_ticket_booking.enums.EventStatus;
import com.rakshitdembla.event_ticket_booking.enums.SeatStatus;
import com.rakshitdembla.event_ticket_booking.exception.InvalidBookingException;
import com.rakshitdembla.event_ticket_booking.exception.SeatAlreadyBookedException;
import com.rakshitdembla.event_ticket_booking.exception.SeatLockedException;
import com.rakshitdembla.event_ticket_booking.mapper.BookingMapper;
import com.rakshitdembla.event_ticket_booking.mapper.TicketMapper;
import com.rakshitdembla.event_ticket_booking.repository.BookingRepository;
import com.rakshitdembla.event_ticket_booking.repository.EventRepository;
import com.rakshitdembla.event_ticket_booking.repository.PaymentRepository;
import com.rakshitdembla.event_ticket_booking.repository.SeatRepository;
import com.rakshitdembla.event_ticket_booking.repository.TicketRepository;
import com.rakshitdembla.event_ticket_booking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private RazorpayService razorpayService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBookingThrowsWhenSeatIsAlreadyBooked() {
        User user = User.builder().id(1L).build();
        Event event = openEvent();
        Seat bookedSeat = Seat.builder().id(10L).seatNumber("A1").event(event).status(SeatStatus.BOOKED).build();
        CreateBookingRequest request = new CreateBookingRequest(event.getId(), List.of(10L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(seatRepository.findAllByIdForUpdate(List.of(10L))).thenReturn(List.of(bookedSeat));

        assertThatThrownBy(() -> bookingService.createBooking(request, 1L))
                .isInstanceOf(SeatAlreadyBookedException.class);
    }

    @Test
    void createBookingThrowsWhenSeatIsCurrentlyLockedBySomeoneElse() {
        User user = User.builder().id(1L).build();
        Event event = openEvent();
        Seat lockedSeat = Seat.builder().id(11L).seatNumber("A2").event(event).status(SeatStatus.LOCKED).build();
        CreateBookingRequest request = new CreateBookingRequest(event.getId(), List.of(11L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(seatRepository.findAllByIdForUpdate(List.of(11L))).thenReturn(List.of(lockedSeat));

        assertThatThrownBy(() -> bookingService.createBooking(request, 1L))
                .isInstanceOf(SeatLockedException.class);
    }

    @Test
    void createBookingThrowsOnDuplicateSeatIdsInTheSameRequest() {
        User user = User.builder().id(1L).build();
        Event event = openEvent();
        CreateBookingRequest request = new CreateBookingRequest(event.getId(), List.of(12L, 12L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> bookingService.createBooking(request, 1L))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void createBookingThrowsWhenEventIsCancelled() {
        User user = User.builder().id(1L).build();
        Event event = Event.builder().id(1L).status(EventStatus.CANCELLED).build();
        CreateBookingRequest request = new CreateBookingRequest(1L, List.of(1L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> bookingService.createBooking(request, 1L))
                .isInstanceOf(InvalidBookingException.class);
    }

    private Event openEvent() {
        return Event.builder()
                .id(1L)
                .status(EventStatus.UPCOMING)
                .bookingStart(LocalDateTime.now().minusHours(1))
                .bookingEnd(LocalDateTime.now().plusHours(1))
                .build();
    }
}
