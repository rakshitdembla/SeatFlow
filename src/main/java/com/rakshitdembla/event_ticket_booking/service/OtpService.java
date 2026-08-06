package com.rakshitdembla.event_ticket_booking.service;

import com.rakshitdembla.event_ticket_booking.enums.OtpPurpose;
import com.rakshitdembla.event_ticket_booking.exception.InvalidOtpException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.expiration-minutes}")
    private long otpExpirationMinutes;

    public String generateAndStoreOtp(OtpPurpose purpose, String email) {
        String otp = String.valueOf(100000 + secureRandom.nextInt(900000));
        redisTemplate.opsForValue().set(buildKey(purpose, email), otp, Duration.ofMinutes(otpExpirationMinutes));
        return otp;
    }

    public void validateOtp(OtpPurpose purpose, String email, String otp) {
        String key = buildKey(purpose, email);
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new InvalidOtpException("Invalid or expired OTP.");
        }

        redisTemplate.delete(key);
    }

    private String buildKey(OtpPurpose purpose, String email) {
        return "otp:" + purpose.name().toLowerCase() + ":" + email;
    }
}
