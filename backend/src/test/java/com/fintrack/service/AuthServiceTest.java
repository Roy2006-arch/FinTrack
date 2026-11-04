package com.fintrack.service;

import com.fintrack.dto.Requests.*;
import com.fintrack.entity.*;
import com.fintrack.exception.ConflictException;
import com.fintrack.repository.*;
import com.fintrack.security.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository users;
    @Mock CategoryRepository categories;
    @Mock AuthSessionRepository sessions;
    @Mock JwtService jwt;
    private AuthService service;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    @BeforeEach void setup() { service = new AuthService(users, categories, sessions, encoder, jwt); }
    private void tokenSetup() { when(jwt.expiry()).thenReturn(Instant.now().plusSeconds(3600)); when(jwt.issue(anyLong(), anyString(), any())).thenReturn("signed-token"); }
    @Test void registrationHashesPasswordNormalizesEmailAndSeedsCategories() {
        when(users.save(any())).thenAnswer(inv -> { User user = inv.getArgument(0); ReflectionTestUtils.setField(user, "id", 1L); return user; }); tokenSetup();
        var result = service.register(new Register(" Test User ", "TEST@example.com", "StrongPass123"));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class); verify(users).save(captor.capture());
        assertEquals("test@example.com", captor.getValue().getEmail());
        assertNotEquals("StrongPass123", captor.getValue().getPassword()); assertTrue(encoder.matches("StrongPass123", captor.getValue().getPassword()));
        assertEquals("Test User", result.user().name()); assertEquals("signed-token", result.token());
        verify(categories).saveAll(argThat(list -> { int count = 0; for (Category ignored : list) count++; return count == 7; }));
        verify(sessions).save(any(AuthSession.class));
    }
    @Test void duplicateRegistrationFailsWithoutWritingUser() {
        when(users.existsByEmail("test@example.com")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.register(new Register("Test", "test@example.com", "StrongPass123")));
        verify(users, never()).save(any());
    }
    @Test void loginChecksBcryptAndIssuesSession() {
        User user = new User("Test", "test@example.com", encoder.encode("StrongPass123")); ReflectionTestUtils.setField(user, "id", 9L);
        when(users.findByEmail("test@example.com")).thenReturn(Optional.of(user)); tokenSetup();
        assertEquals(9L, service.login(new Login("test@example.com", "StrongPass123")).user().id());
        verify(sessions).save(any());
    }
    @Test void unknownEmailAndWrongPasswordGiveSameError() {
        when(users.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        var absent = assertThrows(BadCredentialsException.class, () -> service.login(new Login("unknown@example.com", "wrong")));
        when(users.findByEmail("test@example.com")).thenReturn(Optional.of(new User("Test", "test@example.com", encoder.encode("StrongPass123"))));
        var wrong = assertThrows(BadCredentialsException.class, () -> service.login(new Login("test@example.com", "wrong")));
        assertEquals(absent.getMessage(), wrong.getMessage()); verify(sessions, never()).save(any());
    }
    @Test void logoutRevokesOnlyCurrentSession() { service.logout(new CurrentUser(1L, "session-id")); verify(sessions).deleteById("session-id"); }
}
