package com.rakshitdembla.event_ticket_booking.exception;

public class OtpRequestTooSoonException extends RuntimeException {

    public OtpRequestTooSoonException(String message) {
        super(message);
    }
}
