package com.rakshitdembla.event_ticket_booking.service;

import com.rakshitdembla.event_ticket_booking.dto.request.CreateBookingRequest;
import com.rakshitdembla.event_ticket_booking.dto.response.BookingResponse;
import com.rakshitdembla.event_ticket_booking.entity.Booking;
import com.rakshitdembla.event_ticket_booking.entity.Event;
import com.rakshitdembla.event_ticket_booking.entity.Payment;
import com.rakshitdembla.event_ticket_booking.entity.Seat;
import com.rakshitdembla.event_ticket_booking.entity.User;
import com.rakshitdembla.event_ticket_booking.enums.BookingStatus;
import com.rakshitdembla.event_ticket_booking.enums.EventStatus;
import com.rakshitdembla.event_ticket_booking.enums.PaymentStatus;
import com.rakshitdembla.event_ticket_booking.enums.SeatStatus;
import com.rakshitdembla.event_ticket_booking.exception.InvalidBookingException;
import com.rakshitdembla.event_ticket_booking.exception.ResourceNotFoundException;
import com.rakshitdembla.event_ticket_booking.exception.SeatAlreadyBookedException;
import com.rakshitdembla.event_ticket_booking.exception.SeatLockedException;
import com.rakshitdembla.event_ticket_booking.mapper.BookingMapper;
import com.rakshitdembla.event_ticket_booking.repository.BookingRepository;
import com.rakshitdembla.event_ticket_booking.repository.EventRepository;
import com.rakshitdembla.event_ticket_booking.repository.PaymentRepository;
import com.rakshitdembla.event_ticket_booking.repository.SeatRepository;
import com.rakshitdembla.event_ticket_booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final String REFERENCE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SEAT_LOCK_MINUTES = 5;

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found."));

        validateBookingWindow(event);

        Set<Long> uniqueSeatIds = new LinkedHashSet<>(request.getSeatIds());
        if (uniqueSeatIds.size() != request.getSeatIds().size()) {
            throw new InvalidBookingException("Duplicate seats in request.");
        }

        List<Seat> seats = seatRepository.findAllByIdForUpdate(new ArrayList<>(uniqueSeatIds));

        if (seats.size() != uniqueSeatIds.size()) {
            throw new ResourceNotFoundException("One or more selected seats do not exist.");
        }

        for (Seat seat : seats) {
            validateSeat(seat, event);
        }

        BigDecimal totalAmount = seats.stream()
                .map(seat -> seat.getSeatCategory().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Booking booking = Booking.builder()
                .bookingReference(generateUniqueBookingReference())
                .user(user)
                .event(event)
                .totalAmount(totalAmount)
                .bookingStatus(BookingStatus.PENDING)
                .bookedAt(LocalDateTime.now())
                .build();
        bookingRepository.save(booking);

        LocalDateTime lockExpiry = LocalDateTime.now().plusMinutes(SEAT_LOCK_MINUTES);
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.LOCKED);
            seat.setLockedBy(user);
            seat.setLockedUntil(lockExpiry);
            seat.setBooking(booking);
        }
        seatRepository.saveAll(seats);

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(totalAmount)
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        return bookingMapper.toResponse(booking, seats, payment.getStatus());
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getOwnBookings(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable)
                .map(booking -> bookingMapper.toResponse(
                        booking,
                        seatRepository.findByBookingIdOrderBySeatNumberAsc(booking.getId()),
                        paymentRepository.findByBookingId(booking.getId()).map(Payment::getStatus).orElse(null)));
    }

    @Transactional(readOnly = true)
    public BookingResponse getOwnBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));

        if (!booking.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Booking not found.");
        }

        List<Seat> seats = seatRepository.findByBookingIdOrderBySeatNumberAsc(bookingId);
        PaymentStatus paymentStatus = paymentRepository.findByBookingId(bookingId).map(Payment::getStatus).orElse(null);

        return bookingMapper.toResponse(booking, seats, paymentStatus);
    }

    private void validateBookingWindow(Event event) {
        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.COMPLETED) {
            throw new InvalidBookingException("This event is not open for booking.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(event.getBookingStart()) || now.isAfter(event.getBookingEnd())) {
            throw new InvalidBookingException("Booking is not open for this event right now.");
        }
    }

    private void validateSeat(Seat seat, Event event) {
        if (!seat.getEvent().getId().equals(event.getId())) {
            throw new InvalidBookingException("Seat " + seat.getSeatNumber() + " does not belong to this event.");
        }
        if (seat.getStatus() == SeatStatus.BOOKED) {
            throw new SeatAlreadyBookedException("Seat " + seat.getSeatNumber() + " is already booked.");
        }
        if (seat.getStatus() == SeatStatus.LOCKED) {
            throw new SeatLockedException("Seat " + seat.getSeatNumber() + " is currently locked by another user.");
        }
    }

    private String generateUniqueBookingReference() {
        String reference;
        do {
            reference = "BK-" + randomAlphanumeric(6);
        } while (bookingRepository.existsByBookingReference(reference));
        return reference;
    }

    private String randomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(REFERENCE_CHARS.charAt(secureRandom.nextInt(REFERENCE_CHARS.length())));
        }
        return sb.toString();
    }
}
