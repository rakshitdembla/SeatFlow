package com.rakshitdembla.event_ticket_booking.controller;

import com.rakshitdembla.event_ticket_booking.dto.request.PaymentFailedRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.VerifyPaymentRequest;
import com.rakshitdembla.event_ticket_booking.dto.response.ApiMessageResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.BookingResponse;
import com.rakshitdembla.event_ticket_booking.security.UserPrincipal;
import com.rakshitdembla.event_ticket_booking.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/verify")
    public ResponseEntity<BookingResponse> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(paymentService.verifyPayment(request, principal.getId()));
    }

    @PostMapping("/failed")
    public ResponseEntity<ApiMessageResponse> markPaymentFailed(@Valid @RequestBody PaymentFailedRequest request,
                                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(paymentService.markPaymentFailed(request, principal.getId()));
    }
}
