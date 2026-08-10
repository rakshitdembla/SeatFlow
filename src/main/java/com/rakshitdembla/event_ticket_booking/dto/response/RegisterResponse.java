package com.rakshitdembla.event_ticket_booking.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResponse {

    private String message;
    private boolean newAccount;
}
