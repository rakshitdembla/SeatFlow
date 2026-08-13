package com.rakshitdembla.event_ticket_booking.service;

import com.rakshitdembla.event_ticket_booking.dto.request.RegisterRequest;
import com.rakshitdembla.event_ticket_booking.dto.response.RegisterResponse;
import com.rakshitdembla.event_ticket_booking.entity.User;
import com.rakshitdembla.event_ticket_booking.enums.OtpPurpose;
import com.rakshitdembla.event_ticket_booking.exception.EmailAlreadyExistsException;
import com.rakshitdembla.event_ticket_booking.repository.RefreshTokenRepository;
import com.rakshitdembla.event_ticket_booking.repository.UserRepository;
import com.rakshitdembla.event_ticket_booking.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerWithNewEmailCreatesUserAndSendsVerificationOtp() {
        RegisterRequest request = new RegisterRequest("Test User", "new@example.com", "Password123");
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123")).thenReturn("hashed-password");
        when(otpService.generateAndStoreOtp(OtpPurpose.EMAIL_VERIFICATION, "new@example.com")).thenReturn("123456");

        RegisterResponse response = authService.register(request);

        assertThat(response.isNewAccount()).isTrue();
        verify(userRepository).save(any(User.class));
        verify(emailService).sendOtpEmail("new@example.com", "123456", "Verify your SeatFlow account");
    }

    @Test
    void registerWithExistingVerifiedEmailThrowsConflict() {
        RegisterRequest request = new RegisterRequest("Test User", "taken@example.com", "Password123");
        User existingUser = User.builder().email("taken@example.com").emailVerified(true).build();
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerWithExistingUnverifiedEmailResendsOtpInsteadOfCreatingNewUser() {
        RegisterRequest request = new RegisterRequest("Test User", "pending@example.com", "Password123");
        User existingUser = User.builder().email("pending@example.com").emailVerified(false).build();
        when(userRepository.findByEmail("pending@example.com")).thenReturn(Optional.of(existingUser));
        when(otpService.generateAndStoreOtp(OtpPurpose.EMAIL_VERIFICATION, "pending@example.com")).thenReturn("654321");

        RegisterResponse response = authService.register(request);

        assertThat(response.isNewAccount()).isFalse();
        verify(userRepository, never()).save(any());
        verify(otpService).enforceResendCooldown(OtpPurpose.EMAIL_VERIFICATION, "pending@example.com");
    }
}
