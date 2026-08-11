package com.rakshitdembla.event_ticket_booking.service;

import com.rakshitdembla.event_ticket_booking.entity.Booking;
import com.rakshitdembla.event_ticket_booking.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendOtpEmail(String toEmail, String otp, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText("Your verification code is: " + otp + "\n\nThis code will expire in 10 minutes.");
        mailSender.send(message);
    }

    public void sendBookingConfirmationEmail(String toEmail, Booking booking, List<Ticket> tickets) {
        String ticketLines = tickets.stream()
                .map(ticket -> " - " + ticket.getSeat().getSeatNumber() + " (" + ticket.getSeat().getSeatCategory().getName()
                        + ") | Ticket Code: " + ticket.getUniqueTicketCode())
                .collect(Collectors.joining("\n"));

        String body = "Your booking is confirmed!\n\n"
                + "Booking Reference: " + booking.getBookingReference() + "\n"
                + "Event: " + booking.getEvent().getTitle() + "\n"
                + "Venue: " + booking.getEvent().getVenueName() + ", " + booking.getEvent().getCity() + "\n"
                + "Total Paid: " + booking.getTotalAmount() + "\n\n"
                + "Your tickets:\n" + ticketLines + "\n\n"
                + "See you there!";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Booking Confirmed - " + booking.getBookingReference());
        message.setText(body);
        mailSender.send(message);
    }
}
