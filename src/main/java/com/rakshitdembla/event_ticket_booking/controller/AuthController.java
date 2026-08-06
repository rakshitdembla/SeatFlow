package com.rakshitdembla.event_ticket_booking.controller;

import com.rakshitdembla.event_ticket_booking.dto.request.ForgotPasswordRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.LoginRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.RefreshTokenRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.RegisterRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.ResetPasswordRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.VerifyEmailRequest;
import com.rakshitdembla.event_ticket_booking.dto.response.ApiMessageResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.AuthResponse;
import com.rakshitdembla.event_ticket_booking.security.UserPrincipal;
import com.rakshitdembla.event_ticket_booking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiMessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiMessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(authService.verifyEmail(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiMessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiMessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiMessageResponse> logout(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.logout(principal.getId()));
    }
}
