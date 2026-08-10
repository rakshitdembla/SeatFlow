package com.rakshitdembla.event_ticket_booking.service;

import com.rakshitdembla.event_ticket_booking.enums.OtpPurpose;
import com.rakshitdembla.event_ticket_booking.exception.InvalidOtpException;
import com.rakshitdembla.event_ticket_booking.exception.OtpRequestTooSoonException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final long RESEND_COOLDOWN_SECONDS = 120;

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.expiration-minutes}")
    private long otpExpirationMinutes;

    public String generateAndStoreOtp(OtpPurpose purpose, String email) {
        String otp = String.valueOf(100000 + secureRandom.nextInt(900000));
        redisTemplate.opsForValue().set(buildOtpKey(purpose, email), otp, Duration.ofMinutes(otpExpirationMinutes));
        redisTemplate.opsForValue().set(buildCooldownKey(purpose, email), "1", Duration.ofSeconds(RESEND_COOLDOWN_SECONDS));
        return otp;
    }

    public void enforceResendCooldown(OtpPurpose purpose, String email) {
        Long remainingSeconds = redisTemplate.getExpire(buildCooldownKey(purpose, email), TimeUnit.SECONDS);
        if (remainingSeconds != null && remainingSeconds > 0) {
            throw new OtpRequestTooSoonException(
                    "Please wait " + remainingSeconds + " seconds before requesting a new OTP.");
        }
    }

    public void validateOtp(OtpPurpose purpose, String email, String otp) {
        String key = buildOtpKey(purpose, email);
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new InvalidOtpException("Invalid or expired OTP.");
        }

        redisTemplate.delete(key);
    }

    private String buildOtpKey(OtpPurpose purpose, String email) {
        return "otp:" + purpose.name().toLowerCase() + ":" + email;
    }

    private String buildCooldownKey(OtpPurpose purpose, String email) {
        return "otp:cooldown:" + purpose.name().toLowerCase() + ":" + email;
    }
}
