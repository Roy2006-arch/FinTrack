package com.fintrack.service;
import com.fintrack.dto.Requests.*;
import com.fintrack.dto.Views.AuthView;
import com.fintrack.entity.*;
import com.fintrack.exception.ConflictException;
import com.fintrack.mapper.FinanceMapper;
import com.fintrack.repository.*;
import com.fintrack.security.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service @Transactional
public class AuthService {
    private final UserRepository users;
    private final CategoryRepository categories;
    private final AuthSessionRepository sessions;
    private final PasswordEncoder passwords;
    private final JwtService jwt;
    private final String dummyHash;
    public AuthService(UserRepository users, CategoryRepository categories, AuthSessionRepository sessions, PasswordEncoder passwords, JwtService jwt) {
        this.users = users; this.categories = categories; this.sessions = sessions; this.passwords = passwords; this.jwt = jwt;
        this.dummyHash = passwords.encode(UUID.randomUUID().toString());
    }
    public AuthView register(Register input) {
        String email = input.email().strip().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) throw new ConflictException("Email is already registered");
        User user = users.save(new User(input.name().strip(), email, passwords.encode(input.password())));
        categories.saveAll(List.of(new Category(user, "Salary", CategoryType.INCOME), new Category(user, "Freelance", CategoryType.INCOME), new Category(user, "Food", CategoryType.EXPENSE), new Category(user, "Transport", CategoryType.EXPENSE), new Category(user, "Shopping", CategoryType.EXPENSE), new Category(user, "Bills", CategoryType.EXPENSE), new Category(user, "Other", CategoryType.BOTH)));
        return authenticate(user);
    }
    public AuthView login(Login input) {
        Optional<User> found = users.findByEmail(input.email().strip().toLowerCase(Locale.ROOT));
        boolean valid = passwords.matches(input.password(), found.map(User::getPassword).orElse(dummyHash));
        if (!valid || found.isEmpty()) throw new BadCredentialsException("Email or password is incorrect");
        return authenticate(found.get());
    }
    private AuthView authenticate(User user) {
        String id = UUID.randomUUID().toString(); Instant expires = jwt.expiry();
        sessions.save(new AuthSession(id, user, expires));
        return new AuthView(jwt.issue(user.getId(), id, expires), expires, FinanceMapper.user(user));
    }
    public void logout(CurrentUser user) { sessions.deleteById(user.sessionId()); }
    @Scheduled(fixedDelay = 3600000) public void removeExpiredSessions() { sessions.deleteByExpiresAtBefore(Instant.now()); }
}
