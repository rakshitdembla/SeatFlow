package com.rakshitdembla.event_ticket_booking.security;

import com.rakshitdembla.event_ticket_booking.entity.User;
import com.rakshitdembla.event_ticket_booking.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String TEST_SECRET = "lgydzITpfsQCHUunjUs5/0QTWLkFdHE6pea36zph2UA=";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 900000L);
    }

    @Test
    void generatedTokenCarriesTheUsersEmailAsSubject() {
        User user = User.builder().id(1L).email("test@example.com").role(UserRole.USER).build();

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractEmail(token)).isEqualTo("test@example.com");
    }

    @Test
    void tokenIsValidWhenSubjectMatchesTheGivenUser() {
        User user = User.builder().id(1L).email("test@example.com").role(UserRole.USER).build();
        String token = jwtService.generateAccessToken(user);

        UserPrincipal principal = new UserPrincipal(user);

        assertThat(jwtService.isTokenValid(token, principal)).isTrue();
    }

    @Test
    void tokenIsInvalidWhenCheckedAgainstADifferentUser() {
        User owner = User.builder().id(1L).email("owner@example.com").role(UserRole.USER).build();
        String token = jwtService.generateAccessToken(owner);

        User someoneElse = User.builder().id(2L).email("someone-else@example.com").role(UserRole.USER).build();
        UserPrincipal otherPrincipal = new UserPrincipal(someoneElse);

        assertThat(jwtService.isTokenValid(token, otherPrincipal)).isFalse();
    }
}
