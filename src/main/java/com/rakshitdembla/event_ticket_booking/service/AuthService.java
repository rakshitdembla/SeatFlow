package com.rakshitdembla.event_ticket_booking.service;

import com.rakshitdembla.event_ticket_booking.dto.request.ForgotPasswordRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.LoginRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.RefreshTokenRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.RegisterRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.ResendOtpRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.ResetPasswordRequest;
import com.rakshitdembla.event_ticket_booking.dto.request.VerifyEmailRequest;
import com.rakshitdembla.event_ticket_booking.dto.response.ApiMessageResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.AuthResponse;
import com.rakshitdembla.event_ticket_booking.dto.response.RegisterResponse;
import com.rakshitdembla.event_ticket_booking.entity.RefreshToken;
import com.rakshitdembla.event_ticket_booking.entity.User;
import com.rakshitdembla.event_ticket_booking.enums.OtpPurpose;
import com.rakshitdembla.event_ticket_booking.enums.UserRole;
import com.rakshitdembla.event_ticket_booking.enums.UserStatus;
import com.rakshitdembla.event_ticket_booking.exception.AccountAlreadyVerifiedException;
import com.rakshitdembla.event_ticket_booking.exception.EmailAlreadyExistsException;
import com.rakshitdembla.event_ticket_booking.exception.EmailNotVerifiedException;
import com.rakshitdembla.event_ticket_booking.exception.InvalidRefreshTokenException;
import com.rakshitdembla.event_ticket_booking.exception.ResourceNotFoundException;
import com.rakshitdembla.event_ticket_booking.repository.RefreshTokenRepository;
import com.rakshitdembla.event_ticket_booking.repository.UserRepository;
import com.rakshitdembla.event_ticket_booking.security.JwtService;
import com.rakshitdembla.event_ticket_booking.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (existingUser != null) {
            if (existingUser.isEmailVerified()) {
                throw new EmailAlreadyExistsException("An account with this email already exists.");
            }

            otpService.enforceResendCooldown(OtpPurpose.EMAIL_VERIFICATION, existingUser.getEmail());
            String otp = otpService.generateAndStoreOtp(OtpPurpose.EMAIL_VERIFICATION, existingUser.getEmail());
            emailService.sendOtpEmail(existingUser.getEmail(), otp, "Verify your SeatFlow account");

            return RegisterResponse.builder()
                    .message("This email is already registered but not verified. We've sent a new verification code.")
                    .newAccount(false)
                    .build();
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();

        userRepository.save(user);

        String otp = otpService.generateAndStoreOtp(OtpPurpose.EMAIL_VERIFICATION, user.getEmail());
        emailService.sendOtpEmail(user.getEmail(), otp, "Verify your SeatFlow account");

        return RegisterResponse.builder()
                .message("Registration successful. Please check your email for the verification code.")
                .newAccount(true)
                .build();
    }

    @Transactional
    public ApiMessageResponse resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (user.isEmailVerified()) {
            throw new AccountAlreadyVerifiedException("This account is already verified. Please log in.");
        }

        otpService.enforceResendCooldown(OtpPurpose.EMAIL_VERIFICATION, user.getEmail());
        String otp = otpService.generateAndStoreOtp(OtpPurpose.EMAIL_VERIFICATION, user.getEmail());
        emailService.sendOtpEmail(user.getEmail(), otp, "Your new SeatFlow verification code");

        return new ApiMessageResponse("A new verification code has been sent to your email.");
    }

    @Transactional
    public ApiMessageResponse verifyEmail(VerifyEmailRequest request) {
        otpService.validateOtp(OtpPurpose.EMAIL_VERIFICATION, request.getEmail(), request.getOtp());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        user.setEmailVerified(true);
        userRepository.save(user);

        return new ApiMessageResponse("Email verified successfully. You can now log in.");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email before logging in.");
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token."));

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new InvalidRefreshTokenException("Refresh token has expired. Please log in again.");
        }

        return issueTokens(storedToken.getUser());
    }

    @Transactional
    public ApiMessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String otp = otpService.generateAndStoreOtp(OtpPurpose.PASSWORD_RESET, user.getEmail());
            emailService.sendOtpEmail(user.getEmail(), otp, "Reset your SeatFlow password");
        });

        return new ApiMessageResponse("If an account exists with this email, a reset code has been sent.");
    }

    @Transactional
    public ApiMessageResponse resetPassword(ResetPasswordRequest request) {
        otpService.validateOtp(OtpPurpose.PASSWORD_RESET, request.getEmail(), request.getOtp());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenRepository.deleteByUserId(user.getId());

        return new ApiMessageResponse("Password reset successful. Please log in with your new password.");
    }

    @Transactional
    public ApiMessageResponse logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        return new ApiMessageResponse("Logged out successfully.");
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = generateRawRefreshToken();
        String tokenHash = hashToken(rawRefreshToken);

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .orElse(RefreshToken.builder().user(user).build());

        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiryDate(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpirationMs)));

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .build();
    }

    private String generateRawRefreshToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
