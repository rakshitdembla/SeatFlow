package com.rakshitdembla.event_ticket_booking.service;

import com.rakshitdembla.event_ticket_booking.dto.request.PaymentFailedRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.VerifyPaymentRequest;
import com.rakshitdembla.event_ticket_booking.dto.response.ApiMessageResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.BookingResponse;
import com.rakshitdembla.event_ticket_booking.entity.Booking;
import com.rakshitdembla.event_ticket_booking.entity.Payment;
import com.rakshitdembla.event_ticket_booking.entity.Seat;
import com.rakshitdembla.event_ticket_booking.entity.Ticket;
import com.rakshitdembla.event_ticket_booking.enums.BookingStatus;
import com.rakshitdembla.event_ticket_booking.enums.PaymentStatus;
import com.rakshitdembla.event_ticket_booking.enums.SeatStatus;
import com.rakshitdembla.event_ticket_booking.enums.TicketStatus;
import com.rakshitdembla.event_ticket_booking.exception.InvalidBookingException;
import com.rakshitdembla.event_ticket_booking.exception.PaymentFailedException;
import com.rakshitdembla.event_ticket_booking.exception.ResourceNotFoundException;
import com.rakshitdembla.event_ticket_booking.mapper.BookingMapper;
import com.rakshitdembla.event_ticket_booking.repository.BookingRepository;
import com.rakshitdembla.event_ticket_booking.repository.PaymentRepository;
import com.rakshitdembla.event_ticket_booking.repository.SeatRepository;
import com.rakshitdembla.event_ticket_booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String TICKET_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final RazorpayService razorpayService;
    private final EmailService emailService;
    private final BookingMapper bookingMapper;
    private final BookingFailureService bookingFailureService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public BookingResponse verifyPayment(VerifyPaymentRequest request, Long userId) {
        Booking booking = getOwnedBooking(request.getBookingId(), userId);
        List<Seat> seats = seatRepository.findByBookingIdOrderBySeatNumberAsc(booking.getId());
        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for this booking."));

        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            return bookingMapper.toResponse(booking, seats, payment);
        }

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingException("This booking is no longer valid.");
        }

        if (!seats.isEmpty() && seats.get(0).getLockedUntil().isBefore(LocalDateTime.now())) {
            bookingFailureService.failBooking(booking, seats, payment);
            throw new InvalidBookingException("Your seat lock has expired. Please start a new booking.");
        }

        boolean signatureValid = razorpayService.verifySignature(
                request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature());

        if (!signatureValid) {
            bookingFailureService.failBooking(booking, seats, payment);
            throw new PaymentFailedException("Payment verification failed.");
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        List<Ticket> tickets = new ArrayList<>();
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.BOOKED);
            seat.setLockedBy(null);
            seat.setLockedUntil(null);

            tickets.add(Ticket.builder()
                    .booking(booking)
                    .seat(seat)
                    .ticketPrice(seat.getSeatCategory().getPrice())
                    .uniqueTicketCode(generateUniqueTicketCode())
                    .status(TicketStatus.VALID)
                    .build());
        }
        seatRepository.saveAll(seats);
        ticketRepository.saveAll(tickets);

        sendConfirmationEmailSafely(booking, tickets);

        return bookingMapper.toResponse(booking, seats, payment);
    }

    @Transactional
    public ApiMessageResponse markPaymentFailed(PaymentFailedRequest request, Long userId) {
        Booking booking = getOwnedBooking(request.getBookingId(), userId);

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingException("This booking is no longer pending.");
        }

        List<Seat> seats = seatRepository.findByBookingIdOrderBySeatNumberAsc(booking.getId());
        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for this booking."));

        bookingFailureService.failBooking(booking, seats, payment);

        return new ApiMessageResponse("Booking cancelled. Your seats have been released.");
    }

    private Booking getOwnedBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));

        if (!booking.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Booking not found.");
        }

        return booking;
    }

    private void sendConfirmationEmailSafely(Booking booking, List<Ticket> tickets) {
        try {
            emailService.sendBookingConfirmationEmail(booking.getUser().getEmail(), booking, tickets);
        } catch (Exception ex) {
            log.warn("Booking {} confirmed but confirmation email failed to send", booking.getBookingReference(), ex);
        }
    }

    private String generateUniqueTicketCode() {
        String code;
        do {
            code = "TKT-" + randomAlphanumeric(8);
        } while (ticketRepository.existsByUniqueTicketCode(code));
        return code;
    }

    private String randomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(TICKET_CODE_CHARS.charAt(secureRandom.nextInt(TICKET_CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
